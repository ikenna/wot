package net.ikenna.wot

import akka.actor.{ ActorRef, OneForOneStrategy, Actor, Props }
import akka.event.Logging
import net.ikenna.wot.BookActor.{ TwitterCountResult, ScrapePageResult, GetBookData }
import org.jsoup.nodes.Document
import net.ikenna.wot.ScrapePageActor.Scrape
import org.jsoup.Jsoup
import java.net.SocketTimeoutException
import akka.actor.SupervisorStrategy.{ Stop, Restart }
import net.ikenna.wot.TwitterActor.GetTwitterCount
import org.openqa.selenium.StaleElementReferenceException

object BookActor {
  def name(book: Book): String = book.bookUrl.replace("https://leanpub.com/", "")

  def props(book: Book): Props = Props(new BookActor(book))

  case class GetBookData()

  case class ScrapePageResult(book: Book, author: Set[Author])

  case class TwitterCountResult(book: Book)

}

class BookActor(initialBook: Book) extends Actor with BookUpdater with ConnectWithRetry {
  implicit val log = Logging(context.system, this)

  override def supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 2) {
    case s: SocketTimeoutException => Restart
    case r: StaleElementReferenceException => Restart
  }

  override def receive: Actor.Receive = {
    case GetBookData() => twitterActor ! GetTwitterCount()
    case TwitterCountResult(updatedBook) => scrapePageActor(updatedBook) ! Scrape()
    case ScrapePageResult(updatedBook2, authors) => persistDataAndStopActor(updatedBook2, authors)
  }

  def twitterActor: ActorRef = context.actorOf(TwitterActor.props(initialBook), TwitterActor.name)

  def scrapePageActor(book: Book): ActorRef = context.actorOf(ScrapePageActor.props(book), ScrapePageActor.name)

  def persistDataAndStopActor(book: Book, authors: Set[Author]) = {
    implicit val jdbcTemplate = Db.prodJdbcTemplateWithName(WotCrawlerApp.dbName)
    Db.insert.book(book)
    authors.map(Db.insert.author)
    context.system.stop(self)
  }
}

object ScrapePageActor {

  case class Scrape()

  def props(book: Book) = Props(new ScrapePageActor(book))

  val name = "ScrapePageActor"

}

class ScrapePageActor(book: Book) extends Actor with BookUpdater {
  val log = Logging(context.system, this)
  implicit val document: Document = Jsoup.connect(book.bookUrl).get()

  override def receive: Actor.Receive = {
    case Scrape() => {
      log.debug("Scraping book data")
      sender() ! ScrapePageResult(getMeta(book), getAuthors(book))
      context.stop(self)
    }
  }

}

object TwitterActor {

  case class GetTwitterCount()

  def props(book: Book) = Props(new TwitterActor(book))

  val name = "TwitterActor"

}

class TwitterActor(book: Book) extends TwitterCountsFetcher with Actor {
  implicit val log = Logging(context.system, this)

  override def receive: Actor.Receive = {
    case GetTwitterCount() => {
      log.info("Getting twitter count from leanpub for " + book.bookUrl)
      val updatedBook = updateWithTwitterCount(book)
      sender() ! TwitterCountResult(updatedBook)
      context.stop(self)
    }
  }

}