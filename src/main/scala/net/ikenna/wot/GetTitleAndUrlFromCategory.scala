package net.ikenna.wot

import akka.event.LoggingAdapter
import org.jsoup.{ Jsoup, Connection }
import java.net.SocketTimeoutException
import scala.util.{ Failure, Success, Try }

trait GetTitleAndUrlFromCategory extends ConnectWithRetry {

  def getBookUrlAndTitleFrom(categories: Seq[Category]): Set[Book] = {
    val result = categories.map(getBookUrlAndTitleFrom).foldRight(Set[Book]()) {
      (current, total) => total ++ current
    }
    log.info(s"Fetching total of ${result.size} books")
    result
  }

  def getBookUrlAndTitleFrom(category: Category): Seq[Book] = {
    val iterator = connectWithRetry(category.url).get.getElementsByClass("book-link").iterator
    var book = Seq[Book]()
    while (iterator.hasNext) {
      val element = iterator.next()
      val bookUrl = "https://leanpub.com" + element.attr("href")
      val title = element.text
      book = book :+ Book(bookUrl, Option(title), None, None, None)
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