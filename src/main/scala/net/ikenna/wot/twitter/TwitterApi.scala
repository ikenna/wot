package net.ikenna.wot.twitter

import twitter4j.{ Query, QueryResult, TwitterFactory, Twitter }
import twitter4j.auth.AccessToken
import net.ikenna.wot.twitter.TwitterSearch.Tweet
import scala.collection.JavaConversions._
import net.ikenna.wot.{ ConnectWithRetry, Author }
import akka.event.{ NoLogging, LoggingAdapter }
import java.util.logging.Logger
import scala.util.{ Failure, Success, Try }

object TwitterApi extends ConnectWithRetry {

  val twitter: Twitter = getTwitterApi()

  def getTwitterApi(): Twitter = {
    val t = TwitterFactory.getSingleton()
    t.setOAuthConsumer(consumerKey, consumerSecret)
    t.setOAuthAccessToken(loadAccessToken)
    t
  }

  def consumerKey = sys.env("TWITTER_CONSUMER_KEY")

  def consumerSecret = sys.env("TWITTER_CONSUMER_SECRET")

  def accessToken = sys.env("TWITTER_ACCESS_TOKEN")

  def accessSecret = sys.env("TWITTER_ACCESS_SECRET")

  def printTwitterCreds = {
    println("consumerKey %s; consuerSecret %s, accessToken %s, accessSecret %s".format(consumerKey, consumerSecret, accessToken, accessSecret))
  }

  def loadAccessToken: AccessToken = new AccessToken(accessToken, accessSecret)

  def search(searchTerm: String): Seq[Tweet] = search(searchTerm, twitter)

  def search(searchTerm: String, twitter: Twitter): Seq[Tweet] = {
    var result: Option[QueryResult] = None
    val query: Query = new Query(searchTerm)
    query.setCount(100)
    var queryOption = Option(query)
    val tweets = collection.mutable.ListBuffer[Tweet]()
    do {
      result = Option(twitter.search(queryOption.get))
      tweets.addAll(result.get.getTweets.map(t => Tweet(t.getId, t.getUser.getScreenName, t.getText, t.getRetweetCount)))
      queryOption = Option(result.get.nextQuery())
    } while (queryOption.isDefined)
    tweets
  }

  override val log: LoggingAdapter = NoLogging

}