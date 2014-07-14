package net.ikenna.wot

import org.scalatest._
import scala.Some
import org.jsoup.nodes.Document
import org.jsoup.Jsoup

class BookMetaUpdaterTest extends FunSuite with Matchers with BeforeAndAfterAll with ShouldMatchers with OptionValues {

  val book1: Book = Book("https://leanpub.com/everydayrailsrspec")
  val book2: Book = Book("https://leanpub.com/codebright")
  val book3: Book = Book("https://leanpub.com/growing-rails")

  implicit val document1: Document = Jsoup.connect(book1.bookUrl).get()
  implicit val document2: Document = Jsoup.connect(book2.bookUrl).get()
  implicit val document3: Document = Jsoup.connect(book3.bookUrl).get()

  test("get meta") {
    val expected = Book("https://leanpub.com/everydayrailsrspec",
      Some("Everyday Rails Testing with RSpec"),
      Some("everydayrailsrspec"),
      Some(BookMeta(Some(4117),
        Some("English Chinese 日本語"), None, Some(145), Some(Price(Some(1400), Some(1900))), None)), None)
    BookMetaUpdater.getMeta(book1) should be(expected)
  }
  test("Book 1 should have 4117 readers") {
    BookMetaUpdater.getReaders(document1).value should be(4117)
  }

  test("Book 2 should have 3993 readers") {
    BookMetaUpdater.getReaders(document2).value should be(3993)
  }

  test("Book 1 should have 145 pages") {
    BookMetaUpdater.getPages(document1).value should be(145)
  }

  test("Book 2 should have 449 pages") {
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

  test("get max price") {
    BookMetaUpdater.getMaxPrice(document1).value should be(1900)
    BookMetaUpdater.getMaxPrice(document2).value should be(3999)
  }
  test("Hashtag for book 1 should be everydayrailsrspec") {
    BookMetaUpdater.getHashtag(document1).value should be("everydayrailsrspec")
  }

  test("Authors url should be https://leanpub.com/u/aaronsumner") {
    BookMetaUpdater.getAuthorUrl(document1) should be(Set("https://leanpub.com/u/aaronsumner"))
  }

  test("Authors url should be https://leanpub.com/u/henning-koch and https://leanpub.com/u/thomas-eisenbarth") {
    BookMetaUpdater.getAuthorUrl(document3) should be(Set("https://leanpub.com/u/henning-koch", "https://leanpub.com/u/thomas-eisenbarth"))
  }

  test("Title  should be Everyday Rails Testing with RSpec") {
    BookMetaUpdater.getTitle(document3) should be(Some("Growing Rails Applications in Practice"))
  }

}
