package net.ikenna.wot

import org.scalatest.FunSuite
import org.springframework.jdbc.datasource._
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.core.io.{ Resource, FileSystemResourceLoader }
import org.springframework.jdbc.datasource.init.{ DatabasePopulatorUtils, ResourceDatabasePopulator }
import scala.collection.JavaConversions._
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.simple.SimpleJdbcInsert

class CrawlerTest extends FunSuite {

  test("All 38 category links are available") {
    assert(Categories().data.size === 38)
  }

  test("Get title link for a given category") {
    assert(Crawler.getBookLinkForCategory(Categories().data.head).size === 45)
    assert(Crawler.linksForCategories(Categories()).size === 600)
  }

}

class H2DatabaseTest extends FunSuite {

  val prodDb = "jdbc:h2:./wotdb"
  val testDb = "jdbc:h2:./test-wotdb"

  implicit val template = createJdbcTemplate(testDb)

  test("test connection to h2") {
    assert(testDbConnection === true)
  }

  test("Create table and insert test data") {
    clearDb
    loadSchema()
    insertBook(Book(title = "Treasure Island", "http://bing.com", "#treasure", bookMetaStub, 2, authorStub))
    val result = template.queryForMap("Select * from BOOK where BOOKURL = ?", "http://bing.com").get("TITLE")
    assert(result === "Treasure Island")
  }

  def insertBook(book: Book)(implicit template: JdbcTemplate): Unit = {
    import book._

    val parameters = new MapSqlParameterSource()
      .addValue("BOOKURL", url)
      .addValue("TITLE", title)
      .addValue("HASHTAG", hashtag)
      .addValue("BOOKMETA", meta.hashCode)
      .addValue("NUMTWEETS", numberOfTweets)
      .addValue("AUTHOR", author.hashCode)

    new SimpleJdbcInsert(template.getDataSource)
      .withTableName("BOOK")
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

  def bookMetaStub = BookMeta(None, None, None, None, None, None)

  def authorStub = Author("", "", "", "")
}

