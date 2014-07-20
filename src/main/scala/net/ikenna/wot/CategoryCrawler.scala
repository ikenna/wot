package net.ikenna.wot

import akka.actor._
import akka.event.Logging
import net.ikenna.wot.BookActor.GetBookData
import scala.collection.immutable.Iterable

object CategoryCrawler {

  case class Crawl()

  case class ReceivedBook(book: Book2)

  case class Tick()

  def props: Props = Props(new CategoryCrawler)

  def name: String = "category"
}

class CategoryCrawler extends Actor with GetTitleAndUrlFromCategory {

  import CategoryCrawler._

  override val akkaLogger = Logging(context.system, this)
  var bookCount: Int = 0
  var received: Set[Book2] = Set()

  override def receive: Actor.Receive = {
    case Crawl => {
      val bookTitles = getBookUrlAndTitleFromCategories
      bookCount = bookTitles.size
      bookTitles.map(getBookData)
    }

    case ReceivedBook(b) => {
      received = received + b
      akkaLogger.info("Received %s books".format(received.size))
    }

    case Tick() => persistDataWhenComplete
  }

  def getBookData(urlAndTitle: BookTitleUrl) = {
    akkaLogger.info("Creating actor for " + urlAndTitle.url)
    val bookActor = context.actorOf(BookActor.props(urlAndTitle), BookActor.name(urlAndTitle))
    bookActor ! GetBookData()
  }

  def persistDataWhenComplete: Unit = {
    if (received.size == bookCount) {
      akkaLogger.info("Persisting data")
      WotJson.serializeToJson(received)
      val onlyEnglishTitles = received.filter(b => b.meta.language.getOrElse("").toLowerCase().contains("english"))
      WotCsvWriter.writeBooksToCsv(onlyEnglishTitles)
      context.system.shutdown()
    } else {
      akkaLogger.info("Not persisted yet. Expected %s , received size %s".format(bookCount, received.size))
    }
  }

  def totalReaders(books: Set[Book2]): Int = {
    books.map(_.meta.readers.getOrElse(0)).sum
  }

}

case class AuthorReaders(authors: Set[String], readers: Int)
