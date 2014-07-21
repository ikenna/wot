package net.ikenna.wot

import akka.actor.{ Actor, Props }
import akka.event.Logging
import com.github.tototoshi.csv.CSVWriter
import scala.io.Source
import java.io.{ File, PrintWriter }
import scala.collection.immutable.Iterable
import net.ikenna.wot.CategoryCrawler.ReceivedBook
import net.ikenna.wot.BookActor.GetBookData
import net.ikenna.wot.readersauthor.BookFollower
import net.ikenna.wot.followersreaders.FollowersReadersCorrelationApp
import net.ikenna.wot.followersreaders.FollowersReadersCorrelationApp.Result
import net.ikenna.wot.builddb.TwitterCountsFetcher

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
      val twitterCount = new TwitterCountsFetcher().getTwitterCount(bookUrlTitle.url)
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

