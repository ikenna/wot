package net.ikenna.wot

import org.scalatest.{ BeforeAndAfterAll, FunSuite }
import org.springframework.jdbc.datasource._
import org.springframework.jdbc.core.{ RowMapper, JdbcTemplate }
import org.springframework.core.io.{ Resource, FileSystemResourceLoader }
import org.springframework.jdbc.datasource.init.{ DatabasePopulatorUtils, ResourceDatabasePopulator }
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

  override def beforeAll {
    clearDb
    loadSchema()
  }

  test("test connection to h2") {
    assert(testDbConnection === true)
  }

  test("Create table and insert test book data") {
    val meta = BookMeta(Some(2), Some("English"), Some(3), Some(50), Some(Price(2556, 3456)), Some(Completeness(Some(60), true)))
    val book: Book = Book(title = "Treasure Island", "http://bing.com", "#treasure", meta, 2, "http://leanpub/jameshillspecialauthor")
    insertBook(book)
    assert(getBook("http://bing.com") === book)

  }

  test("insert test author data") {
    val author: Author = Author("James Hill", "@jameshilltesthandle", "http://bing.com", "http://leanpub/jameshillspecialauthor")
    insertAuthor(author)
    assert(getAuthor("http://leanpub/jameshillspecialauthor") === author)
  }

}

object Db {

  def getBook(url: String)(implicit template: JdbcTemplate): Book = {
    val mapper = new RowMapper[Book] {
      override def mapRow(rs: ResultSet, rowNum: Int): Book =
        Book(rs.getString("TITLE"), rs.getString("BOOKURL"), rs.getString("HASHTAG"),
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
    template.queryForObject("Select * from BOOK where BOOKURL = ?", mapper, Array(url))
  }

  def getAuthor(url: String)(implicit template: JdbcTemplate): Author = {
    val mapper = new RowMapper[Author] {
      override def mapRow(rs: ResultSet, rowNum: Int): Author =
        Author(
          rs.getString("NAME"),
          rs.getString("TWITTERHANDLE"),
          rs.getString("TWITTERURL"),
          rs.getString("AUTHORURL"))
    }
    template.queryForObject("Select * from AUTHOR where AUTHORURL = ?", mapper, Array(url))
  }

  def insertBook(book: Book)(implicit template: JdbcTemplate): Unit = {
    import book._

    val parameters = new MapSqlParameterSource()
      .addValue("BOOKURL", url)
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
      .addValue("COMPLETEPERCENT", meta.completeness.getOrElse(Completeness(None, true)).percent.getOrElse(100))
      .addValue("COMPLETETHRESHOLD", meta.completeness.getOrElse(Completeness(None, true)).aboveThreshold)

    new SimpleJdbcInsert(template.getDataSource)
      .withTableName("BOOK")
      .execute(parameters)
  }

  def insertAuthor(author: Author)(implicit template: JdbcTemplate): Unit = {
    import author._

    val parameters = new MapSqlParameterSource()
      .addValue("AUTHORURL", authorUrl)
      .addValue("NAME", name)
      .addValue("TWITTERHANDLE", twitterHandle)
      .addValue("TWITTERURL", twitterUrl)

    new SimpleJdbcInsert(template.getDataSource)
      .withTableName("AUTHOR")
      .execute(parameters)
  }

  def testDbConnection(implicit template: JdbcTemplate): Boolean = template.queryForMap("Select 1 from dual").get("1") == 1

  def clearDb(implicit template: JdbcTemplate) = template.execute(""" DROP ALL OBJECTS  """)

  def createJdbcTemplate(dbUrl: String) = new JdbcTemplate(new SimpleDriverDataSource(new org.h2.Driver(), dbUrl, "sa", ""))

  def loadSchema(schemaFile: String = "src/resources/wot-schema.sql")(implicit template: JdbcTemplate) {
    val schema: Resource = new FileSystemResourceLoader().getResource(schemaFile)
    val populator = new ResourceDatabasePopulator(schema)
    DatabasePopulatorUtils.execute(populator, template.getDataSource)
  }
}