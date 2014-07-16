package sentiment

import dispatch._
import dispatch.Defaults.executor
import net.ikenna.wot.{ Book, Sentiment, BookTweet }
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{ write, read }
import akka.event.LoggingAdapter

case class SentimentRequest(book: Book, tweets: Seq[BookTweet])

case class SentimentResponse(book: Book, tweets: Seq[BookTweet])

object SentimentApi {

  def request(bookTweets: Option[SentimentRequest])(implicit log: LoggingAdapter): Option[SentimentResponse] = {
    bookTweets.map {
      b =>
        val result = SentimentResponse(b.book, update(b.tweets))
        val logSentiments = result.tweets.map(t => t.sentiment.toString).mkString
        log.info(s"Sentiment for ${result.book.bookUrl} : ${logSentiments}")
        result
    }
  }

  def update(bookTweets: Seq[BookTweet])(implicit log: LoggingAdapter): Seq[BookTweet] = {
    val sData = getSentiment(bookTweets: _*)
    val textToPolarityMap: Map[String, Int] = sData.data.map(e => (e.text -> e.polarity.get)).toMap
    bookTweets.map {
      bookTweet =>
        val polarity = textToPolarityMap.get(bookTweet.tweetText)
        bookTweet.copy(sentiment = Sentiment.from(polarity))
    }
  }

  def getSentiment(tweet: BookTweet*)(implicit log: LoggingAdapter): SData = {
    val tweetData: String = serialize(tweet: _*)
    val resultFuture = httpRequest(tweetData)
    deserialize(resultFuture())
  }

  def httpRequest(tweetData: String): dispatch.Future[String] = {
    val sentimentApi: String = "http://www.sentiment140.com/api/bulkClassifyJson?appid=ikenna4u@gmail.com"
    val request = dispatch.url(sentimentApi) << tweetData
    Http(request OK as.String)
  }

  def deserialize(data: String): SData = {
    implicit val formats = Serialization.formats(NoTypeHints)
    read[SData](data)
  }

  def serialize(tweets: BookTweet*): String = {
    implicit val formats = Serialization.formats(NoTypeHints)
    val entries = tweets.toList.map(t => Entry(t.tweetText, None, None))
    write(SData(entries))
  }

}

case class Polarity(value: Int)

case class Meta(language: String)

case class Entry(text: String, polarity: Option[Int], meta: Option[Meta])

case class SData(data: List[Entry])
