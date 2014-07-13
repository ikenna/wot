package net.ikenna.wot

import akka.actor._
import java.net.SocketTimeoutException
import akka.actor.SupervisorStrategy.{ Escalate, Restart }
import org.jsoup.{ Jsoup, Connection }
import scala.util.Try
import scala.concurrent.duration._
import net.ikenna.wot.BookMetaActor.BookPersisted
import net.ikenna.wot.CategoryActor.StoreAllBooksInCategory
import net.ikenna.wot.BookMetaActor.GetBookMeta
import scala.util.Failure
import scala.Some
import akka.actor.OneForOneStrategy
import net.ikenna.wot.CategoryActor.AllBooksInCategoryPersisted
import scala.util.Success
import akka.actor.Terminated
import akka.event.{ LoggingAdapter, Logging }

object CategoryActor {
  def name(category: Category) = category.url.replace("//", ".").replace(":", "").replace("/", ".")

  val props: Props = Props(new CategoryActor())

  case class StoreAllBooksInCategory(category: Category)

  case class AllBooksInCategoryPersisted(category: Category)

}

class CategoryActor extends Actor with GetTitleAndUrlFromCategory {
  var category: Option[Category] = None
  var toFetch: Set[Book] = Set[Book]()
  var fetched: Set[Book] = Set[Book]()

  val log = Logging(context.system, this)

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = 1 minute) {
      case _: SocketTimeoutException => {
        log.info("There was a SocketTimeoutException. Restarting affected actor")
        Restart
      }
      case _: Exception => Escalate
    }

  override def receive: Actor.Receive = {
    case StoreAllBooksInCategory(category) => onStoreAllBooksInCategory(category)
    case BookPersisted(book) => onBookPersisted(book)
    case Terminated(x) => log.info("Actor stopped " + x)
  }

  def onStoreAllBooksInCategory(category: Category) {
    this.category = Some(category)
    val toFetch = getBookUrlAndTitleFrom(category).toSet
    for (book <- toFetch) {
      val bookMetaActor = context.actorOf(BookMetaActor.props, BookMetaActor.name(book))
      context.watch(bookMetaActor)
      bookMetaActor ! GetBookMeta(book)
    }
  }

  def onBookPersisted(book: Book): Unit = {
    fetched = fetched + book
    if (toFetch.equals(fetched)) {
      context.parent ! AllBooksInCategoryPersisted(category.get)
    }
  }
}

trait GetTitleAndUrlFromCategory extends ConnectWithRetry {

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
}

trait ConnectWithRetry {

  val log: LoggingAdapter

  def connectWithRetry(url: String): Connection = {
    var maxRetry = 3
    var connected: Option[Connection] = tryConnect(url)
    while (maxRetry != 0 && connected.isEmpty) {
      log.debug("Retrying connection to " + url)
      maxRetry = maxRetry - 1
      connected = tryConnect(url)
    }
    connected.getOrElse(throw new SocketTimeoutException())
  }

  def tryConnect(url: String): Option[Connection] = {
    Try(Jsoup.connect(url)) match {
      case Success(connection) => {
        log.debug("Connected to " + url)
        Some(connection)
      }
      case Failure(e) => {
        log.error("Error connecting - " + e.toString)
        None
      }
    }
  }
}