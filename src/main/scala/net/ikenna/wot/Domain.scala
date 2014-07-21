package net.ikenna.wot

object Sentiment extends Enumeration {
  def from(polarity: Option[Int]): Sentiment.Sentiment = {
    if (polarity.isDefined) {
      polarity.get match {
        case 4 => Sentiment.Positive
        case 2 => Sentiment.Neutral
        case 0 => Sentiment.Negative
        case _ => throw new RuntimeException("Unknown sentiment")
      }
    } else {
      Sentiment.Null
    }
  }

  type Sentiment = Value
  val Positive, Negative, Neutral, Null = Value
}

import Sentiment._
import net.ikenna.wot.builddb.Ewom

case class BookTweet(tweetUrl: String,
  bookUrl: String,
  tweetText: String,
  retweetCount: Int,
  sentiment: Sentiment,
  hashtag: String,
  originatorUrl: String,
  byAuthor: Boolean)

case class Author(name: String, twitterHandle: String, twitterUrl: Option[String], authorUrl: String, bookUrl: String)

object Author {
  def twitterHandle(author: Author): Option[String] = author.twitterUrl.map(u => u.replace("https://twitter.com/", ""))
}

case class AuthorTweets(authorUrl: String, tweetText: String, tweetUrl: String, retweetCount: Int)

case class Price(min: Option[Int] = None, max: Option[Int])

case class Completeness(percent: Option[Int], aboveThreshold: Boolean)

case class BookMeta(readers: Option[Int] = None,
  language: Option[String] = None,
  numberOfTranslations: Option[Int] = None,
  numberOfPages: Option[Int] = None,
  price: Option[Price] = None,
  completeness: Option[Completeness] = None)

case class Book(bookUrl: String,
  title: Option[String] = None,
  hashtag: Option[String] = None,
  meta: Option[BookMeta] = None,
  numberOfTweets: Option[Int] = None)

case class Book2(bookUrl: String, title: String, meta: BookMeta, numberOfTweets: Option[Int] = None, authors: Set[Author2])
case class Book3(bookUrl: String, title: String, meta: BookMeta, ewom: Ewom, authors: Set[Author2])

object Book2 {
  def sumOfAllAuthorsFollowers(book: Book2) = {
    book.authors.map(a => a.followerCount.getOrElse(0)).sum.toString
  }
}

object Book {
  def searchTermFor(book: Book): String = book.bookUrl.replace("https://", "").replace("http://", "")
}

