package net.ikenna.wot.twitter

import twitter4j.{ Twitter, QueryResult, Query, TwitterFactory }
import scala.collection.JavaConversions._
import twitter4j.auth.AccessToken
import net.ikenna.wot.{ Sentiment, BookTweet, Db, Author }
import scala.collection.mutable

object TwitterApp extends TwitterApp {

  val twitter: Twitter = TwitterFactory.getSingleton()
  //  val x :List[mutable.Buffer[BookTweet]]=    Db.get.books.map{b =>
  //      val h: String = b.hashtag.get
  //    searchFor(h).getTweets.map{ t=>
  //         BookTweet(t.getSource, b.bookUrl, t.getText, t.getRetweetCount, Sentiment.Null, h, t.getUser.getURL, false)
  //      }
  //    }

  // get all books

  // for each book
  // fetch all tweets for hash tag
  // store them in memory

  // for each stored tweet
  //do sentiment analysis
  //store booktweet in db

  //filter out tweets by authors
  // check that each tweet has unique url

}

trait TwitterApp {

  val twitter: Twitter
  twitter.setOAuthConsumer(sys.env("TWITTER_CONSUMER_KEY"), sys.env("TWITTER_CONSUMER_SECRET"))
  twitter.setOAuthAccessToken(loadAccessToken)

  def searchFor(queryTerm: String): QueryResult = {
    val query = new Query("queryTerm")
    twitter.search(query)
  }
  def loadAccessToken: AccessToken = {
    val token = sys.env("TWITTER_ACCESS_TOKEN")
    val tokenSecret = sys.env("TWITTER_ACCESS_SECRET")
    println(token + " --- " + tokenSecret)
    new AccessToken(token, tokenSecret);
  }

}
