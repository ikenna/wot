package net.ikenna.wot.builddb

import net.ikenna.wot.{ Book, WotLogger }
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.{ ExpectedConditions, WebDriverWait }
import akka.event.LoggingAdapter
import scala.util.{ Failure, Success, Try }
import org.openqa.selenium.By

class TwitterCountsFetcher() extends WotLogger {
  val driver: FirefoxDriver = new FirefoxDriver()
  val waiting: WebDriverWait = new WebDriverWait(driver, 15, 100)

  def getTwitterCount(bookUrl: String)(implicit log: LoggingAdapter): Ewom = {
    val result = Try {
      driver.get(bookUrl)
      Ewom(getTwitterCount, getFacebookCount)
    } match {
      case Success(n) => n
      case Failure(e) => {
        log.error("Could not get Twitter or facebook count for " + bookUrl, e)
        Ewom(None, None)
      }
    }
    driver.quit()
    result
  }

  private def getTwitterCount: Option[Int] = {
    defaultLogger.debug("Page title is: " + driver.getTitle());
    val element = waiting.until(ExpectedConditions.visibilityOfElementLocated(By.className("twitter-share-button")));
    val twitterCount = driver.switchTo().frame(element).findElement(By.className("count-ready")).getText()
    Option(twitterCount.replaceAll("Tweet\\s", "").toInt)
  }

  def getFacebookCount: Option[Int] = {
    val element = waiting.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#end_matter > div > div > ul > li.facebook > div > span > iframe")));
    val facebookCount = driver.switchTo().frame(element).findElement(By.className("pluginCountTextDisconnected")).getText()
    if (facebookCount.isEmpty) None else Option(facebookCount.toInt)
  }

}

case class Ewom(twitter: Option[Int], facebook: Option[Int])