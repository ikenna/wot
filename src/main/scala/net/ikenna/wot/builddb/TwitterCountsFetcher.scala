package net.ikenna.wot.builddb

import net.ikenna.wot.{Book, WotLogger}
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import akka.event.LoggingAdapter
import scala.util.{Failure, Success, Try}
import org.openqa.selenium.By

class TwitterCountsFetcher() extends WotLogger {
  val driver: FirefoxDriver = new FirefoxDriver()
  val waiting: WebDriverWait = new WebDriverWait(driver, 15, 100)

  def getTwitterCount(book: Book)(implicit log: LoggingAdapter): Option[Int] = {
    val result = Try(getTwitterCount(book.bookUrl)) match {
      case Success(n) => n
      case Failure(e) => {
        log.error("Could not get Twitter count for " + book.bookUrl, e)
        None
      }
    }
    driver.quit()
    result
  }

  def getTwitterCount(bookUrl: String): Option[Int] = {
    driver.get(bookUrl)
    defaultLogger.debug("Page title is: " + driver.getTitle());
    val element = waiting.until(ExpectedConditions.visibilityOfElementLocated(By.className("twitter-share-button")));
    val twitterCount = driver.switchTo().frame(element).findElement(By.className("count-ready")).getText()
    Option(twitterCount.replaceAll("Tweet\\s", "").toInt)
  }

  def getFacebookCount(bookUrl: String):Option[Int] = {
    driver.get(bookUrl)
    val element = waiting.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#end_matter > div > div > ul > li.facebook > div > span > iframe")));
    val facebookCount = driver.switchTo().frame(element).findElement(By.className("pluginCountTextDisconnected")).getText()
    if(facebookCount.isEmpty) None else Option(facebookCount.toInt)
  }

}
