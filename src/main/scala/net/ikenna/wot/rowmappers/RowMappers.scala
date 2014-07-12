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
      rs.getString("BOOKURL"),
      Option(rs.getString("TITLE")),
      Option(rs.getString("HASHTAG")),
      Option(BookMeta(
        Option(rs.getInt("READERS")),
        Option(rs.getString("LANG")),
        Option(rs.getInt("NUMTRANS")),
        Option(rs.getInt("NUMPAGES")),
        Option(Price(
          Option(rs.getInt("MINPRICE")),
          Option(rs.getInt("MAXPRICE")))
        ),
        Option(
          Completeness(
            Option(rs.getInt("COMPLETEPERCENT")),
            rs.getBoolean("COMPLETETHRESHOLD")
          )))),
      Option(rs.getInt("NUMTWEETS")),
      Option(rs.getString("AUTHORURL"))
    )
}

object BookTableParameters {

  def apply(book: Book): MapSqlParameterSource = {
    import book._

    val lang: Option[String] = meta.flatMap(_.language)
    val numTrans: Option[Int] = meta.flatMap(_.numberOfTranslations)
    val numPages: Option[Int] = meta.flatMap(_.numberOfPages)
    val minPrice: Option[Int] = meta.flatMap(_.price.flatMap(p => p.min))
    val maxPrice: Option[Int] = meta.flatMap(_.price.flatMap(p => p.max))
    val readers: Option[Int] = meta.flatMap(_.readers)
    val completePercent: Option[Int] = meta.flatMap(_.completeness.flatMap(c => c.percent))
    val completeThreshold: Option[Boolean] = meta.flatMap(_.completeness.map(c => c.aboveThreshold))

    new MapSqlParameterSource().addValue("BOOKURL", bookUrl)
      .addValue("TITLE", title.orNull)
      .addValue("HASHTAG", hashtag.orNull)
      .addValue("NUMTWEETS", numberOfTweets.orNull)
      .addValue("AUTHORURL", authorUrl.orNull)
      .addValue("READERS", readers.orNull)
      .addValue("LANG", lang.orNull)
      .addValue("NUMTRANS", numTrans.orNull)
      .addValue("NUMPAGES", numPages.orNull)
      .addValue("MINPRICE", minPrice.orNull)
      .addValue("MAXPRICE", maxPrice.orNull)
      .addValue("COMPLETEPERCENT", completePercent.orNull)
      .addValue("COMPLETETHRESHOLD", completeThreshold.orNull)
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