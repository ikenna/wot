import org.openqa.selenium.{ WebDriver, By }
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.{ ExpectedConditions, WebDriverWait }
import org.scalatest.{ BeforeAndAfterAll, Matchers, FreeSpec }

class TwitterCountsTest extends FreeSpec with Matchers with BeforeAndAfterAll {

  val driver = new FirefoxDriver()
  val waiting = new WebDriverWait(driver, 15, 100)

  "Features" - {
    "get twitter count" in {
      assert(218 === getTwitterCount(driver)("https://leanpub.com/everydayrailsrspec"))
      assert(661 === getTwitterCount(driver)("https://leanpub.com/codebright"))
    }

  }

  def getTwitterCount(driver: WebDriver)(url: String): Int = {
    driver.get(url)
    println("Page title is: " + driver.getTitle());
    val element = waiting.until(ExpectedConditions.visibilityOfElementLocated(By.className("twitter-share-button")));
    val twitterCount = driver.switchTo().frame(element).findElement(By.className("count-ready")).getText()
    twitterCount.replaceAll("Tweet\\s", "").toInt
  }

  override def afterAll() {
    driver.quit()
  }

}

object TwitterCountsFetcher {

  def apply(titles: Map[Url, Title]): Map[Url, TwitterCounts] = {
    implicit val driver = new FirefoxDriver()
    implicit val waiting = new WebDriverWait(driver, 15, 100)
    val result: Map[Url, TwitterCounts] = titles.transform((u, t) => getTwitterCount(u))
    driver.quit()
    result
  }

  def getTwitterCount(url: Url)(implicit waiting: WebDriverWait, driver: WebDriver): TwitterCounts = {
    driver.get(url.value)
    println("Page title is: " + driver.getTitle());
    val element = waiting.until(ExpectedConditions.visibilityOfElementLocated(By.className("twitter-share-button")));
    val twitterCount = driver.switchTo().frame(element).findElement(By.className("count-ready")).getText()
    TwitterCounts(twitterCount.replaceAll("Tweet\\s", "").toInt)
  }
}

case class TwitterCounts(value: Int)