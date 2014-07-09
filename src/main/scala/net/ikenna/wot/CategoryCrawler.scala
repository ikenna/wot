package net.ikenna.wot

import org.jsoup.Jsoup

trait CategoryCrawler extends App {

  def getBooksFromCategoryPages(categories: Seq[Category]): List[Book] = {
    categories.map { getBooksFromCategoryPage }.foldRight(List[Book]()) { (current, total) => total ++ current }
  }

  def getBooksFromCategoryPage(categoryLink: Category): Seq[Book] = {
    val iterator = Jsoup.connect(categoryLink.value).get.getElementsByClass("book-link").iterator
    var book = Seq[Book]()
    while (iterator.hasNext) {
      val element = iterator.next()
      val bookUrl = "https://leanpub.com" + element.attr("href")
      val title = element.text
      book = book :+ Book(Option(title), bookUrl, None, None, None, None)
    }
    book
  }

}

object CategoryCrawlerApp extends App with CategoryCrawler {
  implicit val jdbcTemplate = Db.prodJdbcTemplate
  getBooksFromCategoryPages(Categories.list).map(Db.insert.book)
}

object Categories {
  val list: Seq[Category] = Seq(
    Category("https://leanpub.com/c/academic"),
    Category("https://leanpub.com/c/agile"),
    Category("https://leanpub.com/c/bcwriters"),
    Category("https://leanpub.com/c/biographies"),
    Category("https://leanpub.com/c/business"),
    Category("https://leanpub.com/c/childrensbooks"),
    Category("https://leanpub.com/c/cookbooks"),
    Category("https://leanpub.com/c/culture"),
    Category("https://leanpub.com/c/diet"),
    Category("https://leanpub.com/c/diy"),
    Category("https://leanpub.com/c/erotica"),
    Category("https://leanpub.com/c/familyandparenting"),
    Category("https://leanpub.com/c/fanfiction"),
    Category("https://leanpub.com/c/fantasy"),
    Category("https://leanpub.com/c/fiction"),
    Category("https://leanpub.com/c/general"),
    Category("https://leanpub.com/c/historical_fiction"),
    Category("https://leanpub.com/c/history"),
    Category("https://leanpub.com/c/horror"),
    Category("https://leanpub.com/c/humor"),
    Category("https://leanpub.com/c/humorandsatire"),
    Category("https://leanpub.com/c/internet"),
    Category("https://leanpub.com/c/music"),
    Category("https://leanpub.com/c/mystery"),
    Category("https://leanpub.com/c/nanowrimo"),
    Category("https://leanpub.com/c/poetry"),
    Category("https://leanpub.com/c/religion"),
    Category("https://leanpub.com/c/romance"),
    Category("https://leanpub.com/c/science_fiction"),
    Category("https://leanpub.com/c/selfhelp"),
    Category("https://leanpub.com/c/serialfiction"),
    Category("https://leanpub.com/c/software"),
    Category("https://leanpub.com/c/sports"),
    Category("https://leanpub.com/c/startups"),
    Category("https://leanpub.com/c/textbooks"),
    Category("https://leanpub.com/c/thriller"),
    Category("https://leanpub.com/c/travel"),
    Category("https://leanpub.com/c/young_adult"))
}

case class Category(value: String)
