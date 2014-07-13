package net.ikenna.wot

import org.scalatest.FunSuite

class CategoryCrawlerTest extends FunSuite with GetTitleAndUrlFromCategory {

  test("All 38 category links are available") {
    assert(Categories.list.size === 38)
  }

  test("Get title link for a given category") {
    assert(getBookUrlAndTitleFrom(Categories.list.head).size === 45)
    assert(getBookUrlAndTitleFrom(Categories.list).size === 600)
  }

}

