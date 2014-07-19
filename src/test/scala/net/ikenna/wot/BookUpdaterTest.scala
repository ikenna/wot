package net.ikenna.wot

import org.scalatest._
import scala.Some

class BookUpdaterTest extends FunSuite with Matchers with BeforeAndAfterAll with ShouldMatchers with OptionValues {

  import BooksForTests._

  val updater1 = new DefaultBookUpdater(BookTitleUrl("https://leanpub.com/everydayrailsrspec", "Every Day Rails Spec"))
  val updater2 = new DefaultBookUpdater(BookTitleUrl("https://leanpub.com/codebright", ""))
  val updater3 = new DefaultBookUpdater(BookTitleUrl("https://leanpub.com/everydayrailsrspec", ""))

  test("Book 1 should have 4130 readers") {
    updater1.getReaders.value should be(4130)
  }

  test("Book 2 should have 4016 readers") {
    updater2.getReaders.value should be(4016)
  }

  test("Book 1 should have 145 pages") {
    updater1.getPages.value should be(145)
  }

  test("Book 2 should have 449 pages") {
    updater2.getPages.value should be(449)
  }

  test("get language") {
    updater1.getLanguage.value should be("English Chinese 日本語")
    updater2.getLanguage.value should be("English Español Serbian 日本語 italiana Turkish Português (Brazillian) Русский язык")
  }

  test("get min price") {
    updater1.getMinPrice.value should be(1400)
    updater2.getMinPrice.value should be(2999)
  }

  test("get max price") {
    updater1.getMaxPrice.value should be(1900)
    updater2.getMaxPrice.value should be(3999)
  }
  test("Hashtag for book 1 should be everydayrailsrspec") {
    updater1.getHashtag.value should be("everydayrailsrspec")
  }

  test("Authors url should be https://leanpub.com/u/aaronsumner") {
    updater1.getAuthorUrls should be(Set("https://leanpub.com/u/aaronsumner"))
  }

  test("Authors url should be https://leanpub.com/u/henning-koch and https://leanpub.com/u/thomas-eisenbarth") {
    updater3.getAuthorUrls should be(Set("https://leanpub.com/u/henning-koch", "https://leanpub.com/u/thomas-eisenbarth"))
  }

  test("Title  should be Everyday Rails Testing with RSpec") {
    updater3.getTitle should be(Some("Growing Rails Applications in Practice"))
  }

  test("Get count") {
    updater3.getCount("https://twitter.com/lunivore") should be(Some(5842))
    updater3.getCount("https://twitter.com/vicapow") should be(Some(1104))
    updater3.getCount("https://twitter.com/sivers") should be(Some(277000))
    updater3.getCount("https://twitter.com/royosherove") should be(Some(11700))
    updater3.getCount("https://twitter.com/JimKitzmiller") should be(Some(65700))
  }


}

object BooksForTests {
  val book1: Book = Book("https://leanpub.com/everydayrailsrspec")
  val book2: Book = Book("https://leanpub.com/codebright")
  val book3: Book = Book("https://leanpub.com/growing-rails")
}