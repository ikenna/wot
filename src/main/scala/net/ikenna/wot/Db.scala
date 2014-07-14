package net.ikenna.wot

import org.springframework.jdbc.datasource._
import org.springframework.jdbc.core.{ BatchPreparedStatementSetter, JdbcTemplate }
import org.springframework.core.io.{ Resource, FileSystemResourceLoader }
import org.springframework.jdbc.datasource.init.{ DatabasePopulatorUtils, ResourceDatabasePopulator }
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import java.sql.PreparedStatement
import scala.collection.JavaConversions._
import net.ikenna.wot.rowmappers._
import org.slf4j.LoggerFactory

object Db {

  object get {
    def books(implicit template: JdbcTemplate): List[Book] = template.query("Select * from BOOK ", BookRowMapper).toList

    def bookTweetsByHashTag(hashTag: String)(implicit template: JdbcTemplate): List[BookTweet] = {
      template.query("Select * from BOOKTWEETS where HASHTAG = ?", Array(hashTag.asInstanceOf[Object]), BookTweetRowMapper).toList
    }

    def book(url: String)(implicit template: JdbcTemplate): Book = {
      template.queryForObject("Select * from BOOK where BOOKURL = ?", BookRowMapper, Array(url))
    }

    def author(url: String)(implicit template: JdbcTemplate): Author = {
      template.queryForObject("Select * from AUTHOR where AUTHORURL = ?", AuthorRowMapper, Array(url))
    }

    def authorTweetByTweetUrl(tweetUrl: String)(implicit template: JdbcTemplate): AuthorTweets = {
      template.queryForObject("Select * from AuthorTweets where TWEETURL = ?", AuthorTweetRowMapper, Array(tweetUrl))
    }

    def authorTweetsByAuthorUrl(authorUrl: String)(implicit template: JdbcTemplate): AuthorTweets = {
      template.queryForObject("Select * from AuthorTweets where AUTHORURL = ?", AuthorTweetRowMapper, Array(authorUrl))
    }

    def bookTweetByTweetUrl(tweetUrl: String)(implicit template: JdbcTemplate): BookTweet = {
      template.queryForObject("Select * from BOOKTWEETS where TWEETURL = ?", BookTweetRowMapper, Array(tweetUrl))
    }

  }

  object insert {
    def bookTweets(tweets: Seq[BookTweet])(implicit template: JdbcTemplate) = {
      val sql = "INSERT INTO BOOKTWEETS(TWEETURL, BOOKURL, TWEETTEXT, RETWEETCOUNT, SENTIMENT, HASHTAG, ORIGINATORURL, BYAUTHOR)" +
        " VALUES  (?, ?,  ?, ?,   ?,  ?,  ?,  ? )"

      object BatchSetter extends BatchPreparedStatementSetter {
        def setValues(ps: PreparedStatement, i: Int): Unit = {
          ps.setString(1, tweets(i).tweetUrl)
          ps.setString(2, tweets(i).bookUrl)
          ps.setString(3, tweets(i).tweetText)
          ps.setInt(4, tweets(i).retweetCount)
          ps.setString(5, tweets(i).sentiment.toString)
          ps.setString(6, tweets(i).hashtag)
          ps.setString(7, tweets(i).originatorUrl)
          ps.setBoolean(8, tweets(i).byAuthor)
        }

        def getBatchSize(): Int = tweets.size
      }

      val result: Array[Int] = template.batchUpdate(sql, BatchSetter)

      WotLogger.info("Batch update rows affected = " + result.sum)
    }

    def book(book: Book)(implicit template: JdbcTemplate): Unit = {
      WotLogger.info(s"Inserting book -- ${book.bookUrl}")
      new SimpleJdbcInsert(template.getDataSource).withTableName("BOOK").execute(BookTableParameters(book))
    }

    def author(author: Author)(implicit template: JdbcTemplate): Unit = {
      new SimpleJdbcInsert(template.getDataSource).withTableName("AUTHOR").execute(AuthorParameters(author))
    }

    def authorTweets(authorTweet: AuthorTweets)(implicit template: JdbcTemplate): Unit = {
      new SimpleJdbcInsert(template.getDataSource).withTableName("AUTHORTWEETS").execute(AuthorTweetsParameter(authorTweet))
    }

    def bookTweet(bookTweets: BookTweet)(implicit template: JdbcTemplate) = {
      new SimpleJdbcInsert(template.getDataSource).withTableName("BOOKTWEETS").execute(BookTweetsParameter(bookTweets))
    }
  }

  def testConnection(implicit template: JdbcTemplate): Boolean = template.queryForList("Select * from BOOK").isEmpty == true

  def clear(implicit template: JdbcTemplate) = template.execute(""" DROP ALL OBJECTS  """)

  def createJdbcTemplate(dbUrl: String) = new JdbcTemplate(new SimpleDriverDataSource(new org.h2.Driver(), dbUrl, "sa", ""))

  def prodJdbcTemplateWithName(dbName: String) = createJdbcTemplate("jdbc:h2:./" + dbName)

  def loadSchema(schemaFile: String = "src/main/resources/wot-schema.sql")(implicit template: JdbcTemplate) {
    val schema: Resource = new FileSystemResourceLoader().getResource(schemaFile)
    val populator = new ResourceDatabasePopulator(schema)
    DatabasePopulatorUtils.execute(populator, template.getDataSource)
  }
}

trait CreateDBName {
  def createDBName: String = {
    val dbName = "prod-wotdb-" + new java.util.Date().toString.replace(" ", "").replace(":", "")
    val logger = LoggerFactory.getLogger("net.ikenna.CreateDBName")
    logger.info("Creating DB with name = " + dbName)
    dbName
  }
}