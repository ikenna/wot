package net.ikenna.wot

import org.jsoup.{ Jsoup, Connection }
import java.net.SocketTimeoutException
import scala.util.{ Failure, Success, Try }
import org.slf4j.{ LoggerFactory, Logger }
import akka.event.LoggingAdapter

trait GetTitleAndUrlFromCategory extends ConnectWithRetry {

  def getBookUrlAndTitleFromCategories: Set[BookTitleUrl] = {
    val result = Categories.list.map(getBookUrlAndTitle).flatten.toSet
    defaultLogger.info(s"Fetching total of ${result.size} books")
    result
  }

  def getBookUrlAndTitle(category: Category): Seq[BookTitleUrl] = {
    val iterator = connectWithRetry(category.url).get.getElementsByClass("book-link").iterator
    var titleUrls = Seq[BookTitleUrl]()
    while (iterator.hasNext) {
      val element = iterator.next()
      val bookUrl = "https://leanpub.com" + element.attr("href")
      val title = element.text
      titleUrls = titleUrls :+ BookTitleUrl(bookUrl, title)
    }
    titleUrls
  }
}

case class BookTitleUrl(url: String, title: String)

trait ConnectWithRetry extends WotLogger {

  def connectWithRetry(url: String): Connection = {
    var maxRetry = 3
    var connected: Option[Connection] = tryConnect(url)
    while (maxRetry != 0 && connected.isEmpty) {
      defaultLogger.debug("Retrying connection to " + url)
      maxRetry = maxRetry - 1
      connected = tryConnect(url)
    }
    connected.getOrElse(throw new SocketTimeoutException())
  }

  def tryConnect(url: String): Option[Connection] = {
    Try(Jsoup.connect(url)) match {
      case Success(connection) => {
        defaultLogger.debug("Connected to " + url)
        Some(connection)
      }
      case Failure(e) => {
        defaultLogger.error("Error connecting to %s . Error message - %s".format(url, e.toString))
        None
      }
    }
  }
}

trait ConnectWithRetryAkka {

  //  val akkaLogger: LoggingAdapter
  def connectWithRetry(url: String): Connection = {
    var maxRetry = 3
    var connected: Option[Connection] = tryConnect(url)
    while (maxRetry != 0 && connected.isEmpty) {
      //      akkaLogger.debug("Retrying connection to " + url)
      maxRetry = maxRetry - 1
      connected = tryConnect(url)
    }
    connected.getOrElse(throw new SocketTimeoutException())
  }

  def tryConnect(url: String): Option[Connection] = {
    Try(Jsoup.connect(url)) match {
      case Success(connection) => {
        //        akkaLogger.debug("Connected to " + url)
        Some(connection)
      }
      case Failure(e) => {
        //        akkaLogger.error("Error connecting to %s . Error message - %s".format(url, e.toString))
        None
      }
    }
  }
}