package net.ikenna.wot.rowmappers

import net.ikenna.wot._
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import net.ikenna.wot.Price
import net.ikenna.wot.Author
import net.ikenna.wot.Completeness
import net.ikenna.wot.BookTweet
import net.ikenna.wot.BookMeta
import net.ikenna.wot.AuthorTweets
import net.ikenna.wot.Book
import scala.collection.JavaConversions._

object BookRowMapper extends RowMapper[Book] {
  override def mapRow(rs: ResultSet, rowNum: Int): Book =
    Book(
      rs.getString("TITLE"),
      rs.getString("BOOKURL"),
      rs.getString("HASHTAG"),
      BookMeta(
        Option(rs.getInt("READERS")),
        Option(rs.getString("LANG")),
        Option(rs.getInt("NUMTRANS")),
        Option(rs.getInt("NUMPAGES")),
        Option(Price(
          rs.getInt("MINPRICE"),
          rs.getInt("MAXPRICE"))),
        Option(
          Completeness(
            Option(rs.getInt("COMPLETEPERCENT")),
            rs.getBoolean("COMPLETETHRESHOLD")
          ))),
      rs.getInt("NUMTWEETS"),
      rs.getString("AUTHORURL"))
}

object BookTableParameters {

  def apply(book: Book): MapSqlParameterSource = {
    import book._

    new MapSqlParameterSource().addValue("BOOKURL", url)
      .addValue("TITLE", title)
      .addValue("HASHTAG", hashtag)
      .addValue("NUMTWEETS", numberOfTweets)
      .addValue("AUTHORURL", authorUrl)
      .addValue("READERS", meta.readers.getOrElse(0))
      .addValue("LANG", meta.language.getOrElse("English"))
      .addValue("NUMTRANS", meta.numberOfTranslations.getOrElse(0))
      .addValue("NUMPAGES", meta.numberOfPages.getOrElse(0))
      .addValue("MINPRICE", meta.price.getOrElse(Price(0, 0)).min)
      .addValue("MAXPRICE", meta.price.getOrElse(Price(0, 0)).max)
      .addValue("COMPLETEPERCENT", meta.completeness.getOrElse(Completeness(None, aboveThreshold = true)).percent.getOrElse(100))
      .addValue("COMPLETETHRESHOLD", meta.completeness.getOrElse(Completeness(None, aboveThreshold = true)).aboveThreshold)
  }
}

object AuthorRowMapper extends RowMapper[Author] {
  override def mapRow(rs: ResultSet, rowNum: Int): Author =
    Author(
      rs.getString("NAME"),
      rs.getString("TWITTERHANDLE"),
      rs.getString("TWITTERURL"),
      rs.getString("AUTHORURL"))
}

object AuthorParameters {

  def apply(author: Author) = {
    import author._
    new MapSqlParameterSource()
      .addValue("AUTHORURL", authorUrl)
      .addValue("NAME", name)
      .addValue("TWITTERHANDLE", twitterHandle)
      .addValue("TWITTERURL", twitterUrl)
  }
}

object BookTweetsParameter extends MapSqlParameterSource {

  def apply(bookTweet: BookTweet): MapSqlParameterSource = {
    import bookTweet._
    new MapSqlParameterSource().addValues(
      Map[String, String](
        "TWEETURL" -> tweetUrl,
        "BOOKURL" -> bookUrl,
        "TWEETTEXT" -> tweetText,
        "RETWEETCOUNT" -> retweetCount.toString,
        "SENTIMENT" -> sentiment.toString,
        "HASHTAG" -> hashtag,
        "ORIGINATORURL" -> originatorUrl,
        "BYAUTHOR" -> byAuthor.toString)
    )
  }

}

object BookTweetRowMapper extends RowMapper[BookTweet] {
  override def mapRow(rs: ResultSet, rowNum: Int): BookTweet = {
    BookTweet(
      rs.getString("TWEETURL"),
      rs.getString("BOOKURL"),
      rs.getString("TWEETTEXT"),
      rs.getInt("RETWEETCOUNT"),
      Sentiment.withName(rs.getString("SENTIMENT")),
      rs.getString("HASHTAG"),
      rs.getString("ORIGINATORURL"),
      rs.getBoolean("BYAUTHOR"))
  }
}

object AuthorTweetsParameter {

  def apply(authorTweet: AuthorTweets): MapSqlParameterSource = {

    import authorTweet._

    new MapSqlParameterSource().addValue("AUTHORURL", authorUrl)
      .addValue("TWEETTEXT", tweetText)
      .addValue("TWEETURL", tweetUrl)
      .addValue("RETWEETCOUNT", retweetCount)
  }
}

object AuthorTweetRowMapper extends RowMapper[AuthorTweets] {
  override def mapRow(rs: ResultSet, rowNum: Int): AuthorTweets = {
    AuthorTweets(
      rs.getString("AUTHORURL"),
      rs.getString("TWEETTEXT"),
      rs.getString("TWEETURL"),
      rs.getInt("RETWEETCOUNT"))
  }
}