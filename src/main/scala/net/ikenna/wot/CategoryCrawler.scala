package net.ikenna.wot

import org.jsoup.{ Connection, Jsoup }
import scala.util.{ Failure, Success, Try }
import java.net.SocketTimeoutException
import akka.actor._
import net.ikenna.wot.CategoryCrawler.Crawl
import net.ikenna.wot.CategoryActor.{ AllBooksInCategoryPersisted, StoreAllBooksInCategory }
import akka.event.Logging
import net.ikenna.wot.BookMetaActor.{ BookPersisted, GetBookMeta }
import org.slf4j.LoggerFactory
import akka.actor.SupervisorStrategy.{ Escalate, Restart }
import scala.concurrent.duration._

object CategoryCrawler {

  case class Crawl()

  def props: Props = Props(new CategoryCrawler)

  def name: String = "category-crawler"
}

class CategoryCrawler extends Actor with ActorLogging {

  import CategoryCrawler._

  var toFetch = Set[Category]()
  var fetched = Set[Category]()

  override def receive: Actor.Receive = {

    case Crawl => {
      log.info("Received message " + Crawl)
      toFetch = Categories.list.toSet
      for (category <- toFetch) {
        val categoryActor = context.actorOf(CategoryActor.props, CategoryActor.name(category))
        log.debug(s"Actor ${categoryActor} created")
        categoryActor ! StoreAllBooksInCategory(category)
      }
    }
    case AllBooksInCategoryPersisted(category) => {
      fetched = fetched + category
      if (toFetch.equals(fetched)) log.info("Finished all categories") else log.info(s"Category ${category.url} finished.")
    }
  }
}

object CategoryActor {
  def name(category: Category) = category.url.replace("//", ".").replace(":", "").replace("/", ".")

  val props: Props = Props(new CategoryActor())

  case class StoreAllBooksInCategory(category: Category)

  case class AllBooksInCategoryPersisted(category: Category)

}

class CategoryActor extends Actor with GetTitleAndUrlFromCategory {
  val log = Logging(context.system, this)
  var category: Option[Category] = None
  var toFetch: Set[Book] = Set[Book]()
  var fetched: Set[Book] = Set[Book]()

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = 1 minute) {
      case _: SocketTimeoutException => {
        log.info("There was a SocketTimeoutException. Restarting affected actor")
        Restart
      }
      case _: Exception => Escalate
    }

  override def receive: Actor.Receive = {
    case StoreAllBooksInCategory(category) => getBookMeta(category)
    case BookPersisted(book) => removeFromCountAndNotifyIfFinished(book)
    case Terminated(x) => log.info("Actor stopped " + x)
  }

  def getBookMeta(category: Category) {
    this.category = Some(category)
    val toFetch = getBookUrlAndTitleFrom(category).toSet
    for (book <- toFetch) {
      val bookMetaActor = context.actorOf(BookMetaActor.props, BookMetaActor.name(book))
      context.watch(bookMetaActor)
      bookMetaActor ! GetBookMeta(book)
    }
  }

  def removeFromCountAndNotifyIfFinished(book: Book): Unit = {
    fetched = fetched + book
    if (toFetch.equals(fetched)) {
      context.parent ! AllBooksInCategoryPersisted(category.get)
    }
  }
}

object BookMetaActor {
  def name(book: Book): String = book.bookUrl.replace("//", ".").replace(":", "").replace("/", ".")

  def props: Props = Props(new BookMetaActor())

  case class GetBookMeta(book: Book)

  case class BookPersisted(book: Book)

}

class BookMetaActor extends Actor with BookMetaUpdater {
  val log = Logging(context.system, this)

  override def receive: Actor.Receive = {
    case GetBookMeta(book) => {
      log.debug("Getting book meta")
      val updatedBook = update(book)
      implicit val jdbcTemplate = Db.prodJdbcTemplateWithName(WotCrawlerApp.dbName)
      Db.insert.book(updatedBook)
      sender() ! BookPersisted(book)
    }
  }
}

trait GetTitleAndUrlFromCategory {

  def getBookUrlAndTitleFrom(categories: Seq[Category]): Set[Book] = {
    categories.map(getBookUrlAndTitleFrom).foldRight(Set[Book]()) {
      (current, total) => total ++ current
    }
  }

  def getBookUrlAndTitleFrom(category: Category): Seq[Book] = {
    val iterator = connectWithRetry(category.url).get.getElementsByClass("book-link").iterator
    var book = Seq[Book]()
    while (iterator.hasNext) {
      val element = iterator.next()
      val bookUrl = "https://leanpub.com" + element.attr("href")
      val title = element.text
      book = book :+ Book(bookUrl, Option(title), None, None, None, None)
    }
    book
  }

  /*Not thread safe*/
  def connectWithRetry(url: String): Connection = {
    var maxRetry = 3
    var connected: Option[Connection] = None
    while (maxRetry != 0 && connected.isEmpty) {
      maxRetry = maxRetry - 1
      connected = tryConnect(url)
    }
    connected.getOrElse(throw new SocketTimeoutException())
  }

  def tryConnect(url: String): Option[Connection] = {
    Try(Jsoup.connect(url)) match {
      case Success(connection) => {
        WotLogger.info("Connected to " + url)
        Some(connection)
      }
      case Failure(e) => {
        WotLogger.error("Error connecting - " + e.toString)
        None
      }
    }
  }
}

trait CreateDBName {
  def createDBName: String = {
    val dbName = "prod-wotdb-" + new java.util.Date().toString.replace(" ", "").replace(":", "")
    val logger = LoggerFactory.getLogger("net.ikenna.CreateDBName")
    logger.info("Creating DB with name = " + dbName)
    dbName
  }
}

object CrawlerApp extends App with GetTitleAndUrlFromCategory with CreateDBName {

  implicit val jdbcTemplate = Db.prodJdbcTemplateWithName(createDBName)
  Db.loadSchema()
  val bookWithUrlAndTitle = getBookUrlAndTitleFrom(Categories.list)
  val booksWithMeta = bookWithUrlAndTitle.map(BookMetaUpdater.update)
  booksWithMeta.map(Db.insert.book)
  WotLogger.info(s"Number of Books found = " + bookWithUrlAndTitle.size)
}

object WotCrawlerApp extends App with CreateDBName {
  val dbName = createDBName
  implicit val jdbcTemplate = Db.prodJdbcTemplateWithName(dbName)
  Db.loadSchema()
  Db.testConnection
  val system = ActorSystem("WotCrawler")
  val categoryCrawler = system.actorOf(CategoryCrawler.props, CategoryCrawler.name)
  categoryCrawler ! Crawl
}

object Categories {
  val list: Seq[Category] = Seq(
    Category("https://leanpub.com/c/academic"),
    Category("https://leanpub.com/c/agile"),
    Category("https://leanpub.com/c/bcwriters"),
    Category("https://leanpub.com/c/biographies"),
    Category("https://leanpub.com/c/business"),
    Category("https://leanpub.com/c/childrensbooks"),
    Category("https://leanpub.com/c/cookbooks"),
    Category("https://leanpub.com/c/culture"),
    Category("https://leanpub.com/c/diet"),
    Category("https://leanpub.com/c/diy"),
    Category("https://leanpub.com/c/erotica"),
    Category("https://leanpub.com/c/familyandparenting"),
    Category("https://leanpub.com/c/fanfiction"),
    Category("https://leanpub.com/c/fantasy"),
    Category("https://leanpub.com/c/fiction"),
    Category("https://leanpub.com/c/general"),
    Category("https://leanpub.com/c/historical_fiction"),
    Category("https://leanpub.com/c/history"),
    Category("https://leanpub.com/c/horror"),
    Category("https://leanpub.com/c/humor"),
    Category("https://leanpub.com/c/humorandsatire"),
    Category("https://leanpub.com/c/internet"),
    Category("https://leanpub.com/c/music"),
    Category("https://leanpub.com/c/mystery"),
    Category("https://leanpub.com/c/nanowrimo"),
    Category("https://leanpub.com/c/poetry"),
    Category("https://leanpub.com/c/religion"),
    Category("https://leanpub.com/c/romance"),
    Category("https://leanpub.com/c/science_fiction"),
    Category("https://leanpub.com/c/selfhelp"),
    Category("https://leanpub.com/c/serialfiction"),
    Category("https://leanpub.com/c/software"),
    Category("https://leanpub.com/c/sports"),
    Category("https://leanpub.com/c/startups"),
    Category("https://leanpub.com/c/textbooks"),
    Category("https://leanpub.com/c/thriller"),
    Category("https://leanpub.com/c/travel"),
    Category("https://leanpub.com/c/young_adult"),

    Category("https://leanpub.com/bestsellers"),
    Category("https://leanpub.com/most_copies"),
    Category("https://leanpub.com/bestsellers_lifetime"),
    Category("https://leanpub.com/most_copies_lifetime"),
    Category("https://leanpub.com/new_releases"),
    Category("https://leanpub.com/just_updated"))
}

case class Category(url: String)
