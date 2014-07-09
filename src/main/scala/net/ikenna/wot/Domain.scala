package net.ikenna.wot

object Sentiment extends Enumeration {
  type Sentiment = Value
  val Positive, Negative, Neutral = Value
}

import Sentiment._

trait BookTweetData {
  val tweetUrl: String
  val bookUrl: String
  val tweetText: String
  val retweetCount: String
  val sentiment: Sentiment
  val hashtag: String
  val originatorUrl: String
}

case class BookTweets(byAuthor: BookTweetsByAuthor, byNonAuthor: BookTweetsByNonAuthor)

case class BookTweetsByAuthor(tweetUrl: String,
  bookUrl: String,
  tweetText: String,
  retweetCount: String,
  sentiment: Sentiment,
  hashtag: String,
  originatorUrl: String) extends BookTweetData

case class BookTweetsByNonAuthor(tweetUrl: String,
  bookUrl: String,
  tweetText: String,
  retweetCount: String,
  sentiment: Sentiment,
  hashtag: String,
  originatorUrl: String) extends BookTweetData

case class Author(name: String, twitterHandle: String, twitterUrl: String, authorUrl: String)

case class AuthorTweets(authorUrl: String, tweetText: String, tweetUrl: String, retweetCount: Int)

case class Price(min: Int, max: Int)

case class Completeness(percent: Option[Int], aboveThreshold: Boolean)

case class BookMeta(readers: Option[Int],
  language: Option[String],
  numberOfTranslations: Option[Int],
  numberOfPages: Option[Int],
  price: Option[Price],
  completeness: Option[Completeness])

case class Book(title: String,
  url: String,
  hashtag: String,
  meta: BookMeta,
  numberOfTweets: Int,
  authorUrl: String)

