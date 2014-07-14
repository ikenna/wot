package net.ikenna.wot

import org.scalatest._
import org.jsoup.nodes.Document
import org.jsoup.Jsoup

class TwitterCountsTest extends FreeSpec with BeforeAndAfterAll with ShouldMatchers {

  val book1: Book = Book("https://leanpub.com/everydayrailsrspec")
  val book2: Book = Book("https://leanpub.com/codebright")

  "Features" - {
    "get twitter count" in {
      TwitterCountsFetcher.updateWithTwitterCount(book1).numberOfTweets.get should be >= (218)
      TwitterCountsFetcher.updateWithTwitterCount(book2).numberOfTweets.get should be >= (661)
    }

  }
}
