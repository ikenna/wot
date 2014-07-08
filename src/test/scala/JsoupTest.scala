import org.jsoup.Jsoup
import org.scalatest.{ Matchers, FreeSpec }
import net.ikenna.wot._

class JsoupTest extends FreeSpec with Matchers {

  "Features" - {
    "fetch all the urls and titles in best seller list" in {
      val link = CategoryLink("https://leanpub.com/most_copies_lifetime")
      println(Crawler.getBookLinkForCategory(link))
    }

    "fetch the number of readers for each title" in {
      assert(1202 === getReaderCount("https://leanpub.com/gettingvalueoutofagileretrospectives"))
      assert(4066 === getReaderCount("https://leanpub.com/everydayrailsrspec"))
    }

  }

  def getReaderCount(url: String): Int = Jsoup.connect(url).get().select("li[class*=reader_count] strong").text.toInt

}
