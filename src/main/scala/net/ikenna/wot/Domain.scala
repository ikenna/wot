package net.ikenna.wot

object Sentiment extends Enumeration {
  type Sentiment = Value
  val Positive, Negative, Neutral = Value
}

import Sentiment._

case class BookTweet(tweetUrl: String,
  bookUrl: String,
  tweetText: String,
  retweetCount: Int,
  sentiment: Sentiment,
  hashtag: String,
  originatorUrl: String,
  byAuthor: Boolean)

case class Author(name: String, twitterHandle: String, twitterUrl: String, authorUrl: String)

case class AuthorTweets(authorUrl: String, tweetText: String, tweetUrl: String, retweetCount: Int)

case class Price(min: Int, max: Int)

case class Completeness(percent: Option[Int], aboveThreshold: Boolean)

case class BookMeta(readers: Option[Int] = None,
  language: Option[String] = None,
  numberOfTranslations: Option[Int] = None,
  numberOfPages: Option[Int] = None,
  price: Option[Price] = None,
  completeness: Option[Completeness] = None)

case class Book(title: Option[String],
  bookUrl: String,
  hashtag: Option[String] = None,
  meta: Option[BookMeta] = None,
  numberOfTweets: Option[Int] = None,
  authorUrl: Option[String] = None,
  categoryUrl: Option[String] = None)

