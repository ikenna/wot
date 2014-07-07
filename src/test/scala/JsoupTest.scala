import org.jsoup.Jsoup
import org.scalatest.{ Matchers, FreeSpec }

class JsoupTest extends FreeSpec with Matchers {

  "Features" - {
    "fetch all the urls and titles in best seller list" in {
      val link = CategoryLink("https://leanpub.com/most_copies_lifetime")
      println(getBookLinkForCategory(link))
    }

    "fetch the number of readers for each title" in {
      assert(1202 === getReaderCount("https://leanpub.com/gettingvalueoutofagileretrospectives"))
      assert(4066 === getReaderCount("https://leanpub.com/everydayrailsrspec"))
    }

  }

  def getBookLinkForCategory(categoryLink: CategoryLink): Map[Url, Title] = {
    val iterator = Jsoup.connect(categoryLink.value).get.getElementsByClass("book-link").iterator
    var aMap = Map[Url, Title]()
    while (iterator.hasNext) {
      val element = iterator.next()
      val url = Url("https://leanpub.com" + element.attr("href"))
      val title = Title(element.text)
      aMap = aMap + (url -> title)
    }
    aMap
  }

  def getReaderCount(url: String): Int = Jsoup.connect(url).get().select("li[class*=reader_count] strong").text.toInt

}
case class CategoryLink(value: String)
case class Url(value: String)
case class Title(value: String)