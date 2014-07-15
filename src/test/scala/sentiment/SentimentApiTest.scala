package sentiment

import org.scalatest.{ Matchers, FunSuite }
import net.ikenna.wot.{ Db, Sentiment, BookTweet }
import akka.event.{ NoLogging, LoggingAdapter }

class SentimentApiTest extends FunSuite with Matchers {

  import SentimentApi._
  import org.scalatest.OptionValues._
  implicit val log: LoggingAdapter = NoLogging

  val tweet: BookTweet = BookTweet("", "", "I love Titanic", 0, Sentiment.Null, "", "", false)
  val tweet2: BookTweet = BookTweet("", "", "I hate Titanic", 0, Sentiment.Null, "", "", false)

  test("polarity of - I love Titanic - should be 4") {
    getSentiment(tweet) should be(SData(List(Entry("I love Titanic", Some(4), Some(Meta("en"))))))
  }

  test("get sentiment for several tweets") {
    getSentiment(tweet, tweet2) should be(
      SData(List(Entry("I love Titanic", Some(4), Some(Meta("en"))), Entry("I hate Titanic", Some(0), Some(Meta("en")))))
    )
  }

  test("Serialise one tweet") {
    serialize(tweet) should be("""{"data":[{"text":"I love Titanic"}]}""")
  }

  test("Serialise multiple tweets") {
    serialize(tweet, tweet2) should be("""{"data":[{"text":"I love Titanic"},{"text":"I hate Titanic"}]}""")
  }

  test("DeSerialise  tweets") {
    deserialize("""{"data":[{"text":"I love Titanic"}]}""") should be(SData(List(Entry("I love Titanic", None, None))))
  }
}
