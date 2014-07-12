package net.ikenna.wot

import org.scalatest._
import scala.Some
import org.jsoup.nodes.Document
import org.jsoup.Jsoup

class BookMetaUpdaterTest extends FunSuite with Matchers with BeforeAndAfterAll with ShouldMatchers with OptionValues {

  val book1: Book = Book("https://leanpub.com/everydayrailsrspec")
  val book2: Book = Book("https://leanpub.com/codebright")

  implicit val document1: Document = Jsoup.connect(book1.bookUrl).get()
  implicit val document2: Document = Jsoup.connect(book2.bookUrl).get()

  test("get meta") {
    val expected = Book("https://leanpub.com/everydayrailsrspec", None, None, Some(BookMeta(Some(4111), Some("English Chinese 日本語"), None, Some(145), Some(Price(Some(1400), Some(1900))), None)), None, None)
    BookMetaUpdater.update(book1) should be(expected)
  }
  test("get readers") {
    BookMetaUpdater.getReaders(document1).value should be(4111)
    BookMetaUpdater.getReaders(document2).value should be(3991)
  }

  test("get pages") {
    BookMetaUpdater.getPages(document1).value should be(145)
    BookMetaUpdater.getPages(document2).value should be(449)
  }

  test("get language") {
    BookMetaUpdater.getLanguage(document1).value should be("English Chinese 日本語")
    BookMetaUpdater.getLanguage(document2).value should be("English Español Serbian 日本語 italiana Turkish Português (Brazillian) Русский язык")
  }

  test("get low price") {
    BookMetaUpdater.getMinPrice(document1).value should be(1400)
    BookMetaUpdater.getMinPrice(document2).value should be(2999)
  }

  test("get get max price") {
    BookMetaUpdater.getMaxPrice(document1).value should be(1900)
    BookMetaUpdater.getMaxPrice(document2).value should be(3999)
  }
}
