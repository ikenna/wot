package net.ikenna.wot

import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.{ ExpectedConditions, WebDriverWait }
import org.openqa.selenium.{ By, WebDriver }
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.util.{ Failure, Success, Try }
import org.jsoup.select.Elements

trait Updater {
  def update(book: Book): Book
}

object TwitterCountsFetcher extends Updater {

  def update(book: Book): Book = {
    implicit val driver = new FirefoxDriver()
    implicit val waiting = new WebDriverWait(driver, 15, 100)
    val result: Book = book.copy(numberOfTweets = getTwitterCount(book.bookUrl))
    driver.quit()
    result
  }

  def getTwitterCount(bookUrl: String)(implicit waiting: WebDriverWait, driver: WebDriver): Option[Int] = {
    driver.get(bookUrl)
    WotLogger.debug("Page title is: " + driver.getTitle());
    val element = waiting.until(ExpectedConditions.visibilityOfElementLocated(By.className("twitter-share-button")));
    val twitterCount = driver.switchTo().frame(element).findElement(By.className("count-ready")).getText()
    Option(twitterCount.replaceAll("Tweet\\s", "").toInt)
  }
}

class ParsingException(msg: String, e: Throwable) extends RuntimeException(msg, e)

object BookMetaUpdater extends BookMetaUpdater
trait BookMetaUpdater extends Updater {

  override def update(book: Book): Book = {
    WotLogger.info(s"Updating meta for ${book.bookUrl}")
    implicit val document: Document = Jsoup.connect(book.bookUrl).get()
    val bookMeta = BookMeta(getReaders, getLanguage, None, getPages, Some(Price(getMinPrice, getMaxPrice)))
    book.copy(meta = Some(bookMeta))
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
}
