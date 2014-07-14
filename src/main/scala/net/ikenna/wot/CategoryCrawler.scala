package net.ikenna.wot

import akka.actor._
import akka.event.Logging
import net.ikenna.wot.BookMetaActor.{ BookPersisted, GetBookMeta }
import scala.concurrent.duration._

object CategoryCrawler {

  case class Crawl()

  def props: Props = Props(new CategoryCrawler)

  def name: String = "category-crawler"
}

class CategoryCrawler extends Actor with GetTitleAndUrlFromCategory {

  import CategoryCrawler._

  val log = Logging(context.system, this)
  var toFetch: Int = 0
  var fetched: Int = 0

  override def receive: Actor.Receive = {
    case Crawl => onCrawl
    case BookPersisted(book) => onBookPersisted(book)
  }

  def onCrawl = {
    log.info("Received message " + Crawl)
    val booksToUpdate = getBookUrlAndTitleFrom(Categories.list)
    toFetch = booksToUpdate.size
    log.info(s"Fetching total of ${toFetch} books")
    for (book <- booksToUpdate) {
      val bookMetaActor = context.actorOf(BookMetaActor.props, BookMetaActor.name(book))
      context.watch(bookMetaActor)
      bookMetaActor ! GetBookMeta(book)
    }
  }

  def onBookPersisted(book: Book): Unit = {
    fetched = fetched + 1
    log.info(s"Fetched ${fetched} books")
    if (toFetch == fetched) {
      log.info("Finished all books. Db - " + WotCrawlerApp.dbName)
      context.system.scheduler.scheduleOnce(3 seconds)(context.system.shutdown())(context.dispatcher)
    }
  }
}