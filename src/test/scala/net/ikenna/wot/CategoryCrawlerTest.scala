package net.ikenna.wot

import org.scalatest.FunSuite

class CategoryCrawlerTest extends FunSuite with CategoryCrawler {

  test("All 38 category links are available") {
    assert(Categories.list.size === 38)
  }

  test("Get title link for a given category") {
    assert(getBooksFromCategoryPage(Categories.list.head).size === 45)
    assert(getBooksFromCategoryPages(Categories.list).size === 600)
  }

}

