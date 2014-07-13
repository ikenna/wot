package net.ikenna.wot

import akka.actor._
import net.ikenna.wot.CategoryActor.{ AllBooksInCategoryPersisted, StoreAllBooksInCategory }
import akka.event.Logging

object CategoryCrawler {

  case class Crawl()

  def props: Props = Props(new CategoryCrawler)

  def name: String = "category-crawler"
}

class CategoryCrawler extends Actor {

  import CategoryCrawler._
  val log = Logging(context.system, this)
  var toFetch = Set[Category]()
  var fetched = Set[Category]()

  override def receive: Actor.Receive = {

    case Crawl => {
      log.info("Received message " + Crawl)
      toFetch = Categories.list.toSet
      for (category <- toFetch) {
        val categoryActor = context.actorOf(CategoryActor.props, CategoryActor.name(category))
        log.debug(s"Actor ${categoryActor} created")
        categoryActor ! StoreAllBooksInCategory(category)
      }
    }
    case AllBooksInCategoryPersisted(category) => {
      fetched = fetched + category
      if (toFetch.equals(fetched)) log.info("Finished all categories") else log.info(s"All books in category ${category.url} persisted. ToFetchSize = ${toFetch.size}. Fetched = ${fetched.size}")
    }
  }
}