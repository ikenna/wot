package net.ikenna.wot.twitter

import net.ikenna.wot.{ Db, Sentiment, Book }
import sentiment.{ SentimentRequest, SentimentApi }
import akka.actor.{ Props, ActorSystem, Actor }
import akka.event.{ LoggingAdapter, Logging }
import net.ikenna.wot.BookTweet
import net.ikenna.wot.twitter.TwitterSearch.SearchTwitter
import org.springframework.jdbc.core.JdbcTemplate

object TwitterApp extends App {
  val dbName = "july14-sentiment"
  val actorSystem = ActorSystem("TwitterApp")
  val twitterSearch = actorSystem.actorOf(TwitterSearch.props, TwitterSearch.name)
  twitterSearch ! SearchTwitter()
}

object TwitterSearch {

  def name: String = "TwitterSearch"

  def props: Props = Props(new TwitterSearch())

  case class SearchTwitter()

  //TODO: filter out tweets by authors

  def getBooksWithWithAtLeastOneTwitterCount(implicit log: LoggingAdapter, template: JdbcTemplate): List[Book] = {
    val result = Db.get.books.filter(_.numberOfTweets.fold(false)(_ >= 1))
    log.info("Number of books with 1 or more tweet count = " + result.size)
    result
  }

  private def createSentimentRequest(queryBook: (Book, Seq[Tweet])): SentimentRequest = {
    val (book, result) = queryBook
    val bookTweets = result.map(r => {
      val tweetUrl = "https://api.twitter.com/1.1/statuses/show/" + r.id + ".json"
      val originatorUrl = "https://twitter.com/" + r.user
      BookTweet(tweetUrl, book.bookUrl, r.tweetText, r.reTweetCount, Sentiment.Null, "delete hashtag field", originatorUrl, false)
    })
    SentimentRequest(book, bookTweets)
  }

  def fetchAllTweetsForBook(book: Book)(implicit log: LoggingAdapter): (Book, Seq[Tweet]) = {
    val searchTerm = Book.searchTermFor(book)
    log.info("Fetching tweets for book " + book.bookUrl + "Searching for " + searchTerm)
    val result = TwitterApi.search(searchTerm)
    log.debug("Search result " + book.toString)
    log.info("Found " + result.size + " tweets")
    (book, result)
  }

  def tweetsForBook(book: Book)(implicit log: LoggingAdapter): SentimentRequest = {
    createSentimentRequest(fetchAllTweetsForBook(book))
  }

  def filterOutRequestsWithTweetCountLessThan(numStatus: Int, s: SentimentRequest): Option[SentimentRequest] = {
    if (s.tweets.size >= numStatus) Option(s) else None
  }

  case class Tweet(id: Long, user: String, tweetText: String, reTweetCount: Int)

}

class TwitterSearch extends Actor {

  import TwitterSearch._
  import scala.concurrent.duration._
  import context.dispatcher

  implicit val log = Logging(context.system, this)
  implicit val template = Db.prodJdbcTemplateWithName(TwitterApp.dbName)

  override def receive = {
    case SearchTwitter() => batch
  }

  def onSearchTwitter = {
    val books = getBooksWithWithAtLeastOneTwitterCount
    for (book <- books) {
      val twitterResult = fetchAllTweetsForBook(book)
      val allSentimentRequest = createSentimentRequest(twitterResult)
      val sentimentRequest = filterOutRequestsWithTweetCountLessThan(5, allSentimentRequest)
      val sentimentResponse = SentimentApi.request(sentimentRequest)
      Db.insert.sentimentResponse(sentimentResponse)
    }

  }

  def processBatch(books: List[Book]) = {
    for (book <- books) {
      val twitterResult = fetchAllTweetsForBook(book)
      val allSentimentRequest = createSentimentRequest(twitterResult)
      val sentimentRequest = filterOutRequestsWithTweetCountLessThan(5, allSentimentRequest)
      val sentimentResponse = SentimentApi.request(sentimentRequest)
      Db.insert.sentimentResponse(sentimentResponse)
    }
  }

  def batch = {
    implicit val template = Db.prodJdbcTemplateWithName(TwitterApp.dbName)
    val iterator = getBooksWithWithAtLeastOneTwitterCount.grouped(179)
    log.info("Grouped books by 179 batch")
    var delay = 5
    while (iterator.hasNext) {
      val batch: List[Book] = iterator.next()
      log.info("Setting up schedule for batch size " + batch.size + " to run in " + delay + " minutes")
      context.system.scheduler.scheduleOnce(delay minutes)(processBatch(batch))
      delay = delay + 15
    }
  }

}

