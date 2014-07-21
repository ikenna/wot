package net.ikenna.wot

import akka.actor.{ Actor, Props }
import akka.event.{ LoggingAdapter, Logging }
import scala.io.Source
import java.io.File
import scala.collection.immutable.Iterable
import net.ikenna.wot.CategoryCrawler.ReceivedBook
import net.ikenna.wot.BookActor.GetBookData
import net.ikenna.wot.followersreaders.FollowersReadersCorrelationApp
import net.ikenna.wot.followersreaders.FollowersReadersCorrelationApp.Result
import net.ikenna.wot.ewom.{ Ewom, EwomFetcher }

object BookActor {
  def name(titleUrl: BookTitleUrl): String = titleUrl.url.replace("https://leanpub.com/", "")

  def props(book: BookTitleUrl): Props = Props(new BookActor(book))

  case class GetBookData()

}

class DefaultEwomFetcher(val akkaLogger: LoggingAdapter) extends EwomFetcher

class BookActor(val bookUrlTitle: BookTitleUrl) extends Actor with BookUpdater {
  override implicit val akkaLogger = Logging(context.system, this)

  def onGetBookData: Unit = {
    val result = try {
      akkaLogger.debug("Getting twitter count from leanpub for " + bookUrlTitle.url)
      //      val ewom = new DefaultEwomFetcher(akkaLogger).getEwom(bookUrlTitle.url)
      val authors = getAuthors(getAuthorUrls)
      Book2(bookUrlTitle.url, bookUrlTitle.title, getMeta2, None, authors)
    } catch {
      case e: Exception => {
        akkaLogger.debug("Fetching book data failed ", e)
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

