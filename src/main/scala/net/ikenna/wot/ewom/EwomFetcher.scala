package net.ikenna.wot.ewom

import net.ikenna.wot.{ Book, WotLogger }
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.{ ExpectedConditions, WebDriverWait }
import akka.event.LoggingAdapter
import scala.util.{ Failure, Success, Try }
import org.openqa.selenium.{ WebDriver, By }
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.safari.SafariDriver

trait EwomFetcher {
  val akkaLogger: LoggingAdapter
  val driver: WebDriver = new FirefoxDriver()
  val waiting: WebDriverWait = new WebDriverWait(driver, 3, 100)

  def getEwom(bookUrl: String): Ewom = {
    val result = Try {
      Ewom(getTwitterCountX(bookUrl), getFacebookCount(bookUrl))
    } match {
      case Success(n) => {
        akkaLogger.debug("Got ewom for  " + bookUrl + " - " + n)
        n
      }
      case Failure(e) => {
        akkaLogger.error("Could not get Twitter or facebook count for " + bookUrl, e)
        Ewom(None, None)
      }
    }
    result
  }

  private def getTwitterCountX(url: String): Option[Int] = {
    driver.get(url)
    akkaLogger.debug("Page title is: " + driver.getTitle());
    val element = waiting.until(ExpectedConditions.visibilityOfElementLocated(By.className("twitter-share-button")));
    val twitterCount = driver.switchTo().frame(element).findElement(By.className("count-ready")).getText()
    Option(twitterCount.replaceAll("Tweet\\s", "").toInt)
  }

  def getFacebookCount(url: String): Option[Int] = {
    driver.get(url)
    val element = waiting.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#end_matter > div > div > ul > li.facebook > div > span > iframe")));
    val facebookCount = driver.switchTo().frame(element).findElement(By.className("pluginCountTextDisconnected")).getText()
    if (facebookCount.isEmpty) None else Option(facebookCount.toInt)
  }

}

object MyTest extends App {
  //   val count = new EwomFetcher().getEwom("https://leanpub.com/vagrantcookbook")
  //  println(count)
}
case class Ewom(twitter: Option[Int], facebook: Option[Int])