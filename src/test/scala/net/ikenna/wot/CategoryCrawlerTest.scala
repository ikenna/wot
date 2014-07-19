package net.ikenna.wot

import org.scalatest.FunSuite
import akka.event.LoggingAdapter

class CategoryCrawlerTest extends FunSuite with GetTitleAndUrlFromCategory {

  val category = Category("https://leanpub.com/c/academic")

  test("All 38 category links are available") {
    assert(Categories.list.size === 44)
  }

  test("Academic category should have 48 titles") {
    assert(getBookUrlAndTitle(category).size === 48)
  }

  test("Fetch title") {
    val title = getBookUrlAndTitle(category).head.title
    assert(title === "Functional Reactive Programming on iOS")
  }

  test("There should be 778 titles in all") {
    assert(getBookUrlAndTitleFromCategories.size === 780)
  }

}

