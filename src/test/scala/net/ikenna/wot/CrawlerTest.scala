package net.ikenna.wot

import org.scalatest.{ ShouldMatchers, BeforeAndAfterAll, FunSuite }

class CrawlerTest extends FunSuite {

  test("All 38 category links are available") {
    assert(Categories().data.size === 38)
  }

  test("Get title link for a given category") {
    assert(Crawler.getBookLinkForCategory(Categories().data.head).size === 45)
    assert(Crawler.linksForCategories(Categories()).size === 600)
  }

}

class H2DatabaseTest extends FunSuite with BeforeAndAfterAll with ShouldMatchers {

  implicit val testDbJdbcTemplate = Db.createJdbcTemplate("jdbc:h2:./test-wotdb")

  override def beforeAll() {
    Db.clear
    Db.loadSchema()
  }

  test("test connection to h2") {
    assert(Db.testConnection === true)
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

  test("bulk insert book tweets") {
    val bookTweet1 = BookTweet("https://twitter.com/ikennacn/status/1", "http://leanpub/nicebook", "A very nice tweet message", 5, Sentiment.Positive, "#nicebook", "https://twitter.com/anoriginator", byAuthor = false)
    val bookTweet2 = BookTweet("https://twitter.com/ikennacn/status/2", "http://leanpub/nicebook", "A very nice tweet message", 5, Sentiment.Positive, "#nicebook", "https://twitter.com/anoriginator", byAuthor = false)
    val tweets: Seq[BookTweet] = Seq(bookTweet1, bookTweet2)
    Db.insert.bookTweets(tweets)
    Db.get.bookTweetsByHashTag("#nicebook") should contain(bookTweet1)
    Db.get.bookTweetsByHashTag("#nicebook") should contain(bookTweet2)
  }
}

