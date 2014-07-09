import net.ikenna.wot._
import org.openqa.selenium.{ WebDriver, By }
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.{ ExpectedConditions, WebDriverWait }
import org.scalatest.{ ShouldMatchers, BeforeAndAfterAll, Matchers, FreeSpec }

class TwitterCountsTest extends FreeSpec with Matchers with BeforeAndAfterAll with ShouldMatchers {

  val driver = new FirefoxDriver()
  val waiting = new WebDriverWait(driver, 15, 100)

  "Features" - {
    "get twitter count" in {
      getTwitterCount(driver)("https://leanpub.com/everydayrailsrspec") should be >= (218)
      getTwitterCount(driver)("https://leanpub.com/codebright") should be >= (661)
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

  def apply(titles: Seq[Book]): Seq[Book] = {
    implicit val driver = new FirefoxDriver()
    implicit val waiting = new WebDriverWait(driver, 15, 100)
    val result: Seq[Book] = titles.map(b => b.copy(numberOfTweets = getTwitterCount(b.bookUrl)))
    driver.quit()
    result
  }

  def getTwitterCount(bookUrl: String)(implicit waiting: WebDriverWait, driver: WebDriver): Option[Int] = {
    driver.get(bookUrl)
    println("Page title is: " + driver.getTitle());
    val element = waiting.until(ExpectedConditions.visibilityOfElementLocated(By.className("twitter-share-button")));
    val twitterCount = driver.switchTo().frame(element).findElement(By.className("count-ready")).getText()
    Option(twitterCount.replaceAll("Tweet\\s", "").toInt)
  }
}
