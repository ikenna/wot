package net.ikenna.wot

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.springframework.jdbc.datasource._
import org.springframework.jdbc.core.{RowMapper, JdbcTemplate}
import org.springframework.core.io.{Resource, FileSystemResourceLoader}
import org.springframework.jdbc.datasource.init.{DatabasePopulatorUtils, ResourceDatabasePopulator}
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import java.sql.ResultSet

class CrawlerTest extends FunSuite {

  test("All 38 category links are available") {
    assert(Categories().data.size === 38)
  }

  test("Get title link for a given category") {
    assert(Crawler.getBookLinkForCategory(Categories().data.head).size === 45)
    assert(Crawler.linksForCategories(Categories()).size === 600)
  }

}

class H2DatabaseTest extends FunSuite with BeforeAndAfterAll {

  import Db._

  val prodDb = "jdbc:h2:./wotdb"
  val testDb = "jdbc:h2:./test-wotdb"

  implicit val template = createJdbcTemplate(testDb)

  override def beforeAll() {
    clearDb
    loadSchema()
  }

  test("test connection to h2") {
    assert(testDbConnection === true)
  }

  test("Create table and insert test book data") {
    val meta = BookMeta(Some(2), Some("English"), Some(3), Some(50), Some(Price(2556, 3456)), Some(Completeness(Some(60), aboveThreshold = true)))
    val book: Book = Book(title = "Treasure Island", "http://bing.com", "#treasure", meta, 2, "http://leanpub/jameshillspecialauthor")
    Db.insert.book(book)
    assert(Db.get.book("http://bing.com") === book)

  }

  test("insert test author data") {
    val author: Author = Author("James Hill", "@jameshilltesthandle", "http://bing.com", "http://leanpub/jameshillspecialauthor")
    Db.insert.author(author)
    assert(Db.get.author("http://leanpub/jameshillspecialauthor") === author)
  }

  test("insert author tweets") {
    val authorTweets = AuthorTweets("http://leanpub/jameshillspecialauthor", "A very nice tweet message", "https://twitter.com/ikennacn/status/198710507283300352", 2)
    Db.insert.authorTweets(authorTweets)
    assert(Db.get.authorTweetByTweetUrl("https://twitter.com/ikennacn/status/198710507283300352") === authorTweets)
    assert(Db.get.authorTweetsByAuthorUrl("http://leanpub/jameshillspecialauthor") === authorTweets)
  }

  test("insert book tweets") {
    val bookTweet = BookTweet("https://twitter.com/ikennacn/status/198710507283300352", "http://leanpub/nicebook", "A very nice tweet message", 5, Sentiment.Positive, "#nicebook", "https://twitter.com/anoriginator", byAuthor = false)
    Db.insert.bookTweet(bookTweet)
    assert(Db.get.bookTweetByTweetUrl("https://twitter.com/ikennacn/status/198710507283300352") === bookTweet)
  }

}

object Db {

  import dbrowmappers._

  object get {

    def book(url: String)(implicit template: JdbcTemplate): Book = {
      template.queryForObject("Select * from BOOK where BOOKURL = ?", new BookRowMapper, Array(url))
    }

    def author(url: String)(implicit template: JdbcTemplate): Author = {
      template.queryForObject("Select * from AUTHOR where AUTHORURL = ?", new AuthorRowMapper, Array(url))
    }

    def authorTweetByTweetUrl(tweetUrl: String)(implicit template: JdbcTemplate): AuthorTweets = {
      template.queryForObject("Select * from AuthorTweets where TWEETURL = ?", new AuthorTweetRowMapper, Array(tweetUrl))
    }

    def authorTweetsByAuthorUrl(authorUrl: String)(implicit template: JdbcTemplate): AuthorTweets = {
      template.queryForObject("Select * from AuthorTweets where AUTHORURL = ?", new AuthorTweetRowMapper, Array(authorUrl))
    }

    def bookTweetByTweetUrl(tweetUrl: String)(implicit template: JdbcTemplate): BookTweet = {
      template.queryForObject("Select * from BOOKTWEETS where TWEETURL = ?", new BookTweetRowMapper, Array(tweetUrl))
    }

  }

  object insert {

    def book(book: Book)(implicit template: JdbcTemplate): Unit = {
      new SimpleJdbcInsert(template.getDataSource).withTableName("BOOK").execute(new BookTableParameters(book))
    }

    def author(author: Author)(implicit template: JdbcTemplate): Unit = {
      new SimpleJdbcInsert(template.getDataSource).withTableName("AUTHOR").execute(new AuthorParameters(author))
    }

    def authorTweets(authorTweet: AuthorTweets)(implicit template: JdbcTemplate): Unit = {
      new SimpleJdbcInsert(template.getDataSource).withTableName("AUTHORTWEETS").execute(new AuthorTweetsParameter(authorTweet))
    }

    def bookTweet(bookTweets: BookTweet)(implicit template: JdbcTemplate) = {
      new SimpleJdbcInsert(template.getDataSource).withTableName("BOOKTWEETS").execute(new BookTweetsParameter(bookTweets))
    }
  }

  def testDbConnection(implicit template: JdbcTemplate): Boolean = template.queryForMap("Select 1 from dual").get("1") == 1

  def clearDb(implicit template: JdbcTemplate) = template.execute( """ DROP ALL OBJECTS  """)

  def createJdbcTemplate(dbUrl: String) = new JdbcTemplate(new SimpleDriverDataSource(new org.h2.Driver(), dbUrl, "sa", ""))

  def loadSchema(schemaFile: String = "src/resources/wot-schema.sql")(implicit template: JdbcTemplate) {
    val schema: Resource = new FileSystemResourceLoader().getResource(schemaFile)
    val populator = new ResourceDatabasePopulator(schema)
    DatabasePopulatorUtils.execute(populator, template.getDataSource)
  }
}

package dbrowmappers {

class BookRowMapper extends RowMapper[Book] {
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

class BookTableParameters(book: Book) extends MapSqlParameterSource {

  import book._

  this.addValue("BOOKURL", url)
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

class AuthorRowMapper extends RowMapper[Author] {
  override def mapRow(rs: ResultSet, rowNum: Int): Author =
    Author(
      rs.getString("NAME"),
      rs.getString("TWITTERHANDLE"),
      rs.getString("TWITTERURL"),
      rs.getString("AUTHORURL"))
}

class AuthorParameters(author: Author) extends MapSqlParameterSource {

  import author._

  addValue("AUTHORURL", authorUrl)
    .addValue("NAME", name)
    .addValue("TWITTERHANDLE", twitterHandle)
    .addValue("TWITTERURL", twitterUrl)

}

class BookTweetsParameter(bookTweets: BookTweet) extends MapSqlParameterSource {

  import bookTweets._

  addValue("TWEETURL", tweetUrl)
    .addValue("BOOKURL", bookUrl)
    .addValue("TWEETTEXT", tweetText)
    .addValue("RETWEETCOUNT", retweetCount)
    .addValue("SENTIMENT", sentiment)
    .addValue("HASHTAG", hashtag)
    .addValue("ORIGINATORURL", originatorUrl)
    .addValue("BYAUTHOR", byAuthor)
}

class BookTweetRowMapper extends RowMapper[BookTweet] {
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

class AuthorTweetsParameter(authorTweet: AuthorTweets) extends MapSqlParameterSource {

  import authorTweet._

  addValue("AUTHORURL", authorUrl)
    .addValue("TWEETTEXT", tweetText)
    .addValue("TWEETURL", tweetUrl)
    .addValue("RETWEETCOUNT", retweetCount)
}

class AuthorTweetRowMapper extends RowMapper[AuthorTweets] {
  override def mapRow(rs: ResultSet, rowNum: Int): AuthorTweets = {
    AuthorTweets(
      rs.getString("AUTHORURL"),
      rs.getString("TWEETTEXT"),
      rs.getString("TWEETURL"),
      rs.getInt("RETWEETCOUNT"))
  }
}

}