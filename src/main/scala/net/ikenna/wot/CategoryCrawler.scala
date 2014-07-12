package net.ikenna.wot

import org.jsoup.{Connection, Jsoup}
import scala.util.{Failure, Success, Try}
import java.net.SocketTimeoutException

trait CategoryCrawler extends App {

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

  def getDbName: String = {
    val dbName = "prod-wotdb-" + new java.util.Date().toString.replace(" ", "").replace(":", "")
    WotLogger.info("Creating DB with name = " + dbName)
    dbName
  }
}

object CategoryCrawlerApp extends App with CategoryCrawler {

  implicit val jdbcTemplate = Db.prodJdbcTemplateWithName(getDbName)
  Db.loadSchema()
  val bookWithUrlAndTitle = getBookUrlAndTitleFrom(Categories.list)
  val booksWithMeta = bookWithUrlAndTitle.map(BookMetaUpdater.update)
  booksWithMeta.map(Db.insert.book)
  WotLogger.info(s"Number of Books found = " + bookWithUrlAndTitle.size)
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
