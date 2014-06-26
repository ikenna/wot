import org.jsoup.Jsoup
import org.scalatest.{ Matchers, FreeSpec }

class JsoupTest extends FreeSpec with Matchers {

  "Features" - {
    //    "fetch all the urls and titles in best seller list" in {
    //      val doc = Jsoup.connect("https://leanpub.com/most_copies_lifetime").get()
    //      val elements = doc.getElementsByClass("book-link")
    //      val iterator = elements.iterator()
    //      var aMap = Map[String, String]()
    //      while (iterator.hasNext) {
    //        val element = iterator.next()
    //        val title: String = element.text()
    //        val url: String = "https://leanpub.com" + element.attr("href")
    //        aMap = aMap + (url -> title)
    //      }
    //      println(s"$aMap")
    //    }

    "fetch the number of readers for each title" in {
      assert(1198 === getReaderCount("https://leanpub.com/gettingvalueoutofagileretrospectives"))
      assert(4066 === getReaderCount("https://leanpub.com/everydayrailsrspec"))
    }

    //    "fetch twitter count" in {
    //      val doc = Jsoup.connect("https://leanpub.com/everydayrailsrspec").get()
    //      val elements = doc.getElementsByClass("twitter-widget-0")
    //      println(s"e = $elements")
    //    }

  }

  def getReaderCount(url: String): Int = Jsoup.connect(url).get().select("li[class*=reader_count] strong").text.toInt

}