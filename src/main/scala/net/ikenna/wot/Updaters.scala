package net.ikenna.wot

import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.{ ExpectedConditions, WebDriverWait }
import org.openqa.selenium.{ By, WebDriver }
import org.jsoup.nodes.Document
import scala.util.{ Failure, Success, Try }
import org.jsoup.select.Elements

object TwitterCountsFetcher {
  implicit val driver = new FirefoxDriver()
  implicit val waiting = new WebDriverWait(driver, 15, 100)

  def updateWithTwitterCount(book: Book): Book = {
    book.copy(numberOfTweets = getTwitterCount(book.bookUrl))
  }

  def getTwitterCount(bookUrl: String)(implicit waiting: WebDriverWait, driver: WebDriver): Option[Int] = {
    driver.get(bookUrl)
    WotLogger.debug("Page title is: " + driver.getTitle());
    val element = waiting.until(ExpectedConditions.visibilityOfElementLocated(By.className("twitter-share-button")));
    val twitterCount = driver.switchTo().frame(element).findElement(By.className("count-ready")).getText()
    Option(twitterCount.replaceAll("Tweet\\s", "").toInt)
  }

  def quit() = driver.quit()

}

class ParsingException(msg: String, e: Throwable) extends RuntimeException(msg, e)

object BookMetaUpdater extends BookMetaUpdater {
}

trait BookMetaUpdater {

  def getTitle(implicit document: Document): Option[String] = Option(document.select("h1[itemprop=name]").text())

  def getMeta(book: Book)(implicit document: Document): Book = {
    WotLogger.info(s"Updating meta for ${book.bookUrl}")
    val bookMeta = BookMeta(getReaders, getLanguage, None, getPages, Some(Price(getMinPrice, getMaxPrice)))
    book.copy(meta = Some(bookMeta), hashtag = getHashtag, title = getTitle)
  }

  def getMaxPrice(implicit document: Document): Option[Int] = {
    val selection: String = "span[itemprop*=highPrice]"
    val select: Elements = document.select(selection)
    if (select.isEmpty) {
      WotLogger.info("No max price for " + document.location())
      None
    } else {
      Try(select.text.replace("$", "").replace(".", "").replace(",", "").replace("+", "").split(" ").head.toInt) match {
        case Success(int) => Option(int)
        case Failure(e) => {
          WotLogger.error(s"Failure parsing ${select.text} at ${document.location}")
          None
        }
      }
    }
  }

  def getMinPrice(implicit document: Document): Option[Int] = {
    val selection: String = "span[itemprop*=lowPrice] "
    val select: Elements = document.select(selection)
    if (select.isEmpty) {
      WotLogger.info("No min price for " + document.location())
      None
    } else {
      Try(select.text.replace("$", "").replace(".", "").replace(",", "").split(" ").head.toInt) match {
        case Success(int) => Option(int)
        case Failure(e) => {
          WotLogger.error(s"Failure parsing ${select.text} at ${document.location}")
          None
        }
      }
    }
  }

  def getLanguage(implicit document: Document): Option[String] = {
    val select: Elements = document.select("ul li[class=language]")
    if (select.isEmpty) {
      WotLogger.error("No language for " + document.location())
      None
    } else {
      Try(select.text.replace("Book language:", "").trim) match {
        case Success(text) if !text.isEmpty => Some(text)
        case Success(text) if text.isEmpty => None
        case Failure(e) => {
          WotLogger.error(s"Failure parsing ${select.text} at ${document.location}")
          None
        }
      }
    }
  }

  def getReaders(implicit document: Document): Option[Int] = {
    val selection: String = "li[class*=reader_count] strong"
    val select: Elements = document.select(selection)
    if (select.isEmpty) None
    else Try(select.text.toInt) match {
      case Success(int) => Option(int)
      case Failure(e) => {
        WotLogger.error(s"Failure parsing ${select.text} at ${document.location}")
        None
      }
    }
  }

  def getPages(implicit document: Document): Option[Int] = {
    val selection: String = "li[class*=page_count] strong"
    val select: Elements = document.select(selection)
    if (select.isEmpty) None
    else Try(select.text.toInt) match {
      case Success(int) => Option(int)
      case Failure(e) => {
        WotLogger.error(s"Failure parsing ${select.text} at ${document.location}")
        None
      }
    }
  }

  def getHashtag(implicit document: Document): Option[String] = {
    Option(document.location.replace("https://leanpub.com/", ""))
  }

  def getAuthorUrl(implicit document: Document): Set[String] = {
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

  def getAuthor(book: Book)(implicit document: Document): Set[Author] = {
    getAuthorUrl.map(url => Author("", "", getAuthorTwitterUrl, url, book.bookUrl))
  }

  def getAuthorTwitterUrl(implicit document: Document): Option[String] = {
    val selection: String = "a[href*=https://twitter.com]"
    val select: Elements = document.select(selection)
    val iterator = select.listIterator()
    while (iterator.hasNext) {
      val attr: String = iterator.next().attr("href")
      if (!"https://twitter.com/share".equals(attr)) {
        return Option(attr)
      }
    }
    None
  }

}
