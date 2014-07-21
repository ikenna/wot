package net.ikenna.wot

import org.scalatest._
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import akka.event.{NoLogging, LoggingAdapter}
import net.ikenna.wot.builddb.TwitterCountsFetcher

class TwitterCountsTest extends FreeSpec with BeforeAndAfterAll with ShouldMatchers {

  val book1: Book = Book("https://leanpub.com/everydayrailsrspec")
  val book2: Book = Book("https://leanpub.com/codebright")
  implicit val log: LoggingAdapter = NoLogging

  "TwitterCountsFetcher should" - {
    "get twitter count" in {
      new TwitterCountsFetcher().getTwitterCount(book1).get should be >= (218)
      new TwitterCountsFetcher().getTwitterCount(book2).get should be >= (661)
    }

  }
}
