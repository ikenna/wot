package sentiment

import dispatch._
import dispatch.Defaults.executor
import net.ikenna.wot.BookTweet
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{ write, read }

object SentimentApi extends App {

  def getSentiment2(tweet: BookTweet*): SData = {
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
