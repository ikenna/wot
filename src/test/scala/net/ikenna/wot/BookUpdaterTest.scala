package net.ikenna.wot

import org.scalatest._
import scala.Some
import org.jsoup.nodes.Document
import org.jsoup.Jsoup

class BookUpdaterTest extends FunSuite with Matchers with BeforeAndAfterAll with ShouldMatchers with OptionValues {

  import BooksForTests._
  implicit val document1: Document = Jsoup.connect(book1.bookUrl).get()
  implicit val document2: Document = Jsoup.connect(book2.bookUrl).get()
  implicit val document3: Document = Jsoup.connect(book3.bookUrl).get()

  test("get meta") {
    val expected = Book("https://leanpub.com/everydayrailsrspec",
      Some("Everyday Rails Testing with RSpec"),
      Some("everydayrailsrspec"),
      Some(BookMeta(Some(4117),
        Some("English Chinese 日本語"), None, Some(145), Some(Price(Some(1400), Some(1900))), None)), None)
    BookUpdater.getMeta(book1)(document1) should be(expected)
  }
  test("Book 1 should have 4117 readers") {
    BookUpdater.getReaders(document1).value should be(4117)
  }

  test("Book 2 should have 3993 readers") {
    BookUpdater.getReaders(document2).value should be(3995)
  }

  test("Book 1 should have 145 pages") {
    BookUpdater.getPages(document1).value should be(145)
  }

  test("Book 2 should have 449 pages") {
    BookUpdater.getPages(document2).value should be(449)
  }

  test("get language") {
    BookUpdater.getLanguage(document1).value should be("English Chinese 日本語")
    BookUpdater.getLanguage(document2).value should be("English Español Serbian 日本語 italiana Turkish Português (Brazillian) Русский язык")
  }

  test("get low price") {
    BookUpdater.getMinPrice(document1).value should be(1400)
    BookUpdater.getMinPrice(document2).value should be(2999)
  }

  test("get max price") {
    BookUpdater.getMaxPrice(document1).value should be(1900)
    BookUpdater.getMaxPrice(document2).value should be(3999)
  }
  test("Hashtag for book 1 should be everydayrailsrspec") {
    BookUpdater.getHashtag(document1).value should be("everydayrailsrspec")
  }

  test("Authors url should be https://leanpub.com/u/aaronsumner") {
    BookUpdater.getAuthorUrl(document1) should be(Set("https://leanpub.com/u/aaronsumner"))
  }

  test("Authors url should be https://leanpub.com/u/henning-koch and https://leanpub.com/u/thomas-eisenbarth") {
    BookUpdater.getAuthorUrl(document3) should be(Set("https://leanpub.com/u/henning-koch", "https://leanpub.com/u/thomas-eisenbarth"))
  }

  test("Title  should be Everyday Rails Testing with RSpec") {
    BookUpdater.getTitle(document3) should be(Some("Growing Rails Applications in Practice"))
  }

  test("Author Twitter url should be should be https://twitter.com/everydayrails") {
    BookUpdater.getAuthorTwitterUrl(document1).value should be("https://twitter.com/everydayrails")
  }

}

object BooksForTests {
  val book1: Book = Book("https://leanpub.com/everydayrailsrspec")
  val book2: Book = Book("https://leanpub.com/codebright")
  val book3: Book = Book("https://leanpub.com/growing-rails")
}