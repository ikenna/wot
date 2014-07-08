package net.ikenna.wot

import org.scalatest.FunSuite

class CrawlerTest extends FunSuite {

  test("All category links available") {
    assert(Categories.data === 23)
  }

}
