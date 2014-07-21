package net.ikenna.wot

import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.{ ExpectedConditions, WebDriverWait }
import org.openqa.selenium.By
import org.jsoup.nodes.Document
import scala.util.{ Failure, Success, Try }
import org.jsoup.select.Elements
import akka.event.LoggingAdapter
import net.ikenna.wot.authorfollower.TwitterAuthorFollowers
import net.ikenna.wot.builddb.TwitterCountsFetcher

class ParsingException(msg: String, e: Throwable) extends RuntimeException(msg, e)

class DefaultBookUpdater(val bookUrlTitle: BookTitleUrl) extends BookUpdater

trait BookUpdater extends ConnectWithRetry {

  val bookUrlTitle: BookTitleUrl
  val document: Document = connectWithRetry(bookUrlTitle.url).get()

  def getTitle: Option[String] = Option(document.select("h1[itemprop=name]").text())

  def getMeta(book: Book): Book = {
    val bookMeta: BookMeta = getMeta2
    book.copy(meta = Some(bookMeta), hashtag = getHashtag, title = getTitle)
  }

  def getMeta2: BookMeta = {
    defaultLogger.info(s"Updating meta for ${document.location()}")
    val bookMeta = BookMeta(getReaders, getLanguage, None, getPages, Some(Price(getMinPrice, getMaxPrice)))
    bookMeta
  }

  def getMaxPrice: Option[Int] = {
    val selection: String = "span[itemprop*=highPrice]"
    val select: Elements = document.select(selection)
    if (select.isEmpty) {
      defaultLogger.debug("No max price for " + document.location())
      None
    } else {
      Try(select.text.replace("$", "").replace(".", "").replace(",", "").replace("+", "").split(" ").head.toInt) match {
        case Success(int) => Option(int)
        case Failure(e) => {
          defaultLogger.error(s"Failure parsing ${select.text} at ${document.location}")
          None
        }
      }
    }
  }

  def getMinPrice: Option[Int] = {
    val selection: String = "span[itemprop*=lowPrice] "
    val select: Elements = document.select(selection)
    if (select.isEmpty) {
      defaultLogger.debug("No min price for " + document.location())
      None
    } else {
      Try(select.text.replace("$", "").replace(".", "").replace(",", "").split(" ").head.toInt) match {
        case Success(int) => Option(int)
        case Failure(e) => {
          defaultLogger.error(s"Failure parsing ${select.text} at ${document.location}")
          None
        }
      }
    }
  }

  def getLanguage: Option[String] = {
    val select: Elements = document.select("ul li[class=language]")
    if (select.isEmpty) {
      defaultLogger.error("No language for " + document.location())
      None
    } else {
      Try(select.text.replace("Book language:", "").trim) match {
        case Success(text) if !text.isEmpty => Some(text)
        case Success(text) if text.isEmpty => None
        case Failure(e) => {
          defaultLogger.error(s"Failure parsing ${select.text} at ${document.location}")
          None
        }
      }
    }
  }

  def getReaders: Option[Int] = {
    val selection: String = "li[class*=reader_count] strong"
    val select: Elements = document.select(selection)
    if (select.isEmpty) None
    else Try(select.text.toInt) match {
      case Success(int) => Option(int)
      case Failure(e) => {
        defaultLogger.error(s"Failure parsing ${select.text} at ${document.location}")
        None
      }
    }
  }

  def getPages: Option[Int] = {
    val selection: String = "li[class*=page_count] strong"
    val select: Elements = document.select(selection)
    if (select.isEmpty) None
    else Try(select.text.toInt) match {
      case Success(int) => Option(int)
      case Failure(e) => {
        defaultLogger.error(s"Failure parsing ${select.text} at ${document.location}")
        None
      }
    }
  }

  def getHashtag: Option[String] = {
    Option(document.location.replace("https://leanpub.com/", ""))
  }

  def getAuthorUrls: Set[String] = {
    val selection: String = "a[href*=/u/]"
    val select: Elements = document.select(selection)
    val buffer = collection.mutable.Buffer[String]()
    val iterator = select.listIterator()
    while (iterator.hasNext) {
      val attr: String = iterator.next().attr("href")
      if (attr.startsWith("https://leanpub.com")) {
        buffer.append(attr)
      }
    }
    buffer.toSet
  }

  def getAuthors(authorUrls: Set[String]): Set[Author2] = {
    val result = authorUrls.map {
      authorUrl =>
        Try {
          val twitterUrl = getTwitterUrl(authorUrl)
          val followerCount = twitterUrl.flatMap(getCount)
          akkaLogger.info("Twitter Count for  %s - %s".format(twitterUrl, followerCount))
          Author2(authorUrl, twitterUrl, followerCount)
        } match {
          case Success(a) => {
            akkaLogger.info("Got author - " + a)
            a
          }
          case Failure(e) => {
            akkaLogger.info("Failure get author - " + authorUrl + " " + e.getMessage)
            Author2(authorUrl, None, None)
          }
        }
    }
    akkaLogger.info("Result of get authors " + result)
    result
  }

  def getTwitterUrl(authorUrl: String): Option[String] = {
    val url = connectWithRetry(authorUrl).get.select("#user_title > small:nth-child(2) > a").attr("href").trim
    if (url.isEmpty) None else Option(url)
  }

  def getCountText(twitterUrl: String): String = {
    connectWithRetry(twitterUrl).get().select("#page-container > div.ProfileCanopy.ProfileCanopy--withNav > div > div.ProfileCanopy-navBar > div > div > div.Grid-cell.u-size3of4 > div > div > ul > li.ProfileNav-item.ProfileNav-item--followers > a > span.ProfileNav-value").text()
  }

  def getCount(twitterUrl: String): Option[Int] = {
    def toNum(text: String): Int = {
      val a = text.replace(",", "")
      if (a.contains("K")) {
        (a.replace("K", "").toDouble * 1000).toInt
      } else {
        a.toInt
      }
    }

    Try(toNum(getCountText(twitterUrl))) match {
      case Success(r) => Option(r)
      case Failure(e) => {
        akkaLogger.error("Error getting count for %s - %s".format(twitterUrl, e.getMessage))
        None
      }
    }
  }

}

case class Author2(authorUrl: String, twitterUrl: Option[String], followerCount: Option[Int])