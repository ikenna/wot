package net.ikenna.wot

import org.scalatest.FunSuite
import akka.event.LoggingAdapter

class CategoryCrawlerTest extends FunSuite with GetTitleAndUrlFromCategory {

  test("All 38 category links are available") {
    assert(Categories.list.size === 44)
  }

  test("Academic category should have 47 titles") {
    assert(getBookUrlAndTitleFrom(Category("https://leanpub.com/c/academic")).size === 47)
  }

  test("There should be 778 titles in all") {
    assert(getBookUrlAndTitleFrom(Categories.list).size === 778)
  }

  override val log: LoggingAdapter = akka.event.NoLogging
}

