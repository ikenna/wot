package net.ikenna.wot

import org.scalatest.{ Matchers, FunSuite }

class BookTest extends FunSuite with Matchers {

  import BooksForTests._

  test("Book search term") {
    Book.searchTermFor(book1) should be("leanpub.com/everydayrailsrspec")
  }
}
