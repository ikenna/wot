package net.ikenna.wot

import akka.actor.{ Actor, Props }
import akka.event.Logging
import net.ikenna.wot.BookActor.GetBookData
import com.github.tototoshi.csv.CSVWriter
import net.ikenna.wot.CategoryCrawler.ReceivedBook
import scala.io.Source
import java.io.{ File, PrintWriter }
import scala.collection.immutable.Iterable
import net.ikenna.wot.readersauthor.BookFollower
import net.ikenna.wot.authorfollower.TwitterAuthorFollowers

object BookActor {
  def name(titleUrl: BookTitleUrl): String = titleUrl.url.replace("https://leanpub.com/", "")

  def props(book: BookTitleUrl): Props = Props(new BookActor(book))

  case class GetBookData()

}

class BookActor(val bookUrlTitle: BookTitleUrl) extends Actor with ConnectWithRetry with BookUpdater {
  override implicit val akkaLogger = Logging(context.system, this)

  def onGetBookData: Unit = {
    val result = try {
      akkaLogger.debug("Getting twitter count from leanpub for " + bookUrlTitle.url)
      val twitterCount = None // TODO: new TwitterCountsFetcher().getTwitterCount(bookUrlTitle.url)
      val authors = getAuthors(getAuthorUrls)
      Book2(bookUrlTitle.url, bookUrlTitle.title, getMeta2, twitterCount, authors)
    } catch {
      case e: Exception => {
        akkaLogger.debug("Fetching book data failed ", e.getLocalizedMessage)
        Book2(bookUrlTitle.url, "Exception - Failed " + e.getLocalizedMessage, BookMeta(), None, Set())
      }
    }
    context.parent ! ReceivedBook(result)
  }

  override def receive = {
    case GetBookData() => onGetBookData
  }

  def persistDataAndStopActor(book: Book) = {
    implicit val jdbcTemplate = Db.prodJdbcTemplateWithName(WotCrawlerApp.dbName)
    Db.insert.book(book)
  }

}

object WotCsvWriter {

  def writeToCsv(lines: List[List[Any]], file: String) = {
    val fileName = file + RunTimeStamp() + ".csv"
    val writer = CSVWriter.open(fileName, append = true)
    writer.writeAll(lines)
    writer.close()
  }

  def writeBooksToCsv(book: Set[Book2]) = {
    val fileName = "books-" + RunTimeStamp() + ".csv"
    val all: List[List[String]] = book.toList.map(getCsvLine)
    val writer = CSVWriter.open(fileName, append = true)
    writer.writeAll(all)
    writer.close()
  }

  def getCsvLine(book: Book2): List[String] = {
    List(book.bookUrl,
      book.meta.readers.getOrElse(0).toString,
      Book2.sumOfAllAuthorsFollowers(book)
    )
  }
}

object WotJson extends WotLogger {

  import org.json4s._
  import org.json4s.jackson.Serialization
  import org.json4s.jackson.Serialization._

  implicit val formats = Serialization.formats(NoTypeHints)

  def serializeToJson(bookFollower: Seq[BookFollower]): Unit = {
    val fileName = "book-follower-" + RunTimeStamp() + ".json"
    val ser = write(bookFollower)
    val writer = new PrintWriter(fileName, "UTF-8");
    writer.println(ser)
    writer.close()
  }

  def serializeToJson(authorReaders: Iterable[AuthorReaders]): Unit = {
    val fileName = "author-reader-" + RunTimeStamp() + ".json"
    val ser = write(authorReaders)
    val writer = new PrintWriter(fileName, "UTF-8");
    writer.println(ser)
    writer.close()
  }
  def serializeToJson(books: Set[Book2]): Unit = {
    val fileName = "books-" + RunTimeStamp() + ".json"

    val ser = write(books)
    val writer = new PrintWriter(fileName, "UTF-8");
    writer.println(ser)
    writer.close()
  }

  def deSerializeBooks(fileName: String): List[Book2] = {
    val jsonFile = Source.fromFile(new File(fileName)).mkString
    assert(new File(fileName).exists())
    read[List[Book2]](jsonFile)
  }

}

object WotAgent {

  import scala.concurrent.ExecutionContext.Implicits.global
  import akka.agent.Agent

  val agent: Agent[Set[Book2]] = Agent(Set[Book2]())
}