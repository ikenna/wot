package net.ikenna.wot.twitter

import org.scalatest.{ Matchers, FunSuite }
import net.ikenna.wot.{ Db, Book }
import akka.event.{ LoggingAdapter, NoLogging }

class TwitterSearchTest extends FunSuite with Matchers {

  import TwitterSearch._
  import net.ikenna.wot.BooksForTests._
  import org.scalatest.OptionValues._

  implicit val log: LoggingAdapter = NoLogging
  implicit val template = Db.prodJdbcTemplateWithName("prod-wotdb-MonJul14031047BST2014") //TODO: use test db instead

  test("Fetch all tweets for book") {
    val (_, result) = fetchAllTweetsForBook(book1)
    result.size should be(6)
  }

  test("Convert tweets to bookTweet") {
    val b = tweetsForBook(book1)
    b.tweets.head.tweetUrl should startWith("https://api.twitter.com/1.1/statuses/show/")
  }

  test("search for books with 1 or more tweets") {
    val booksWithZeroTweets: (Book) => Boolean = {
      book => book.numberOfTweets.fold(true)(n => n == 0)
    }

    val booksWithOneOrMoreTweets: (Book) => Boolean = {
      book => book.numberOfTweets.fold(false)(n => n > 0)
    }
    getBooksWithWithAtLeastOneTwitterCount.filter(booksWithZeroTweets).size should be(0)
    getBooksWithWithAtLeastOneTwitterCount.filter(booksWithOneOrMoreTweets).size should be(510)
  }

  import TwitterSearch._

  test("Book tweets to get sentiments for") {
    val sentimentRequest = tweetsForBook(book1)
    filterOutRequestsWithTweetCountLessThan(6, sentimentRequest).value should be(sentimentRequest)
    filterOutRequestsWithTweetCountLessThan(7, sentimentRequest) should be(None)
  }
}