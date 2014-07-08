package net.ikenna.wot

import org.jsoup.Jsoup

object Crawler {
  def linksForCategories(categories: Categories): Map[Url, Title] = {
    categories.data.map { getBookLinkForCategory }.foldRight(Map[Url, Title]()) { (current, total) => total ++ current }
  }

  def getBookLinkForCategory(categoryLink: CategoryLink): Map[Url, Title] = {
    val iterator = Jsoup.connect(categoryLink.value).get.getElementsByClass("book-link").iterator
    var aMap = Map[Url, Title]()
    while (iterator.hasNext) {
      val element = iterator.next()
      val url = Url("https://leanpub.com" + element.attr("href"))
      val title = Title(element.text)
      aMap = aMap + (url -> title)
    }
    aMap
  }

}

case class Categories(data: Seq[CategoryLink] = List(
  CategoryLink("https://leanpub.com/c/academic"),
  CategoryLink("https://leanpub.com/c/agile"),
  CategoryLink("https://leanpub.com/c/bcwriters"),
  CategoryLink("https://leanpub.com/c/biographies"),
  CategoryLink("https://leanpub.com/c/business"),
  CategoryLink("https://leanpub.com/c/childrensbooks"),
  CategoryLink("https://leanpub.com/c/cookbooks"),
  CategoryLink("https://leanpub.com/c/culture"),
  CategoryLink("https://leanpub.com/c/diet"),
  CategoryLink("https://leanpub.com/c/diy"),
  CategoryLink("https://leanpub.com/c/erotica"),
  CategoryLink("https://leanpub.com/c/familyandparenting"),
  CategoryLink("https://leanpub.com/c/fanfiction"),
  CategoryLink("https://leanpub.com/c/fantasy"),
  CategoryLink("https://leanpub.com/c/fiction"),
  CategoryLink("https://leanpub.com/c/general"),
  CategoryLink("https://leanpub.com/c/historical_fiction"),
  CategoryLink("https://leanpub.com/c/history"),
  CategoryLink("https://leanpub.com/c/horror"),
  CategoryLink("https://leanpub.com/c/humor"),
  CategoryLink("https://leanpub.com/c/humorandsatire"),
  CategoryLink("https://leanpub.com/c/internet"),
  CategoryLink("https://leanpub.com/c/music"),
  CategoryLink("https://leanpub.com/c/mystery"),
  CategoryLink("https://leanpub.com/c/nanowrimo"),
  CategoryLink("https://leanpub.com/c/poetry"),
  CategoryLink("https://leanpub.com/c/religion"),
  CategoryLink("https://leanpub.com/c/romance"),
  CategoryLink("https://leanpub.com/c/science_fiction"),
  CategoryLink("https://leanpub.com/c/selfhelp"),
  CategoryLink("https://leanpub.com/c/serialfiction"),
  CategoryLink("https://leanpub.com/c/software"),
  CategoryLink("https://leanpub.com/c/sports"),
  CategoryLink("https://leanpub.com/c/startups"),
  CategoryLink("https://leanpub.com/c/textbooks"),
  CategoryLink("https://leanpub.com/c/thriller"),
  CategoryLink("https://leanpub.com/c/travel"),
  CategoryLink("https://leanpub.com/c/young_adult")))

case class CategoryLink(value: String)

case class Url(value: String)

case class Title(value: String)