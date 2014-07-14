package net.ikenna.wot

import akka.actor.{ Actor, Props }
import akka.event.Logging
import net.ikenna.wot.BookMetaActor.{ BookPersisted, GetBookMeta }
import org.jsoup.nodes.Document
import org.jsoup.Jsoup

object BookMetaActor {
  def name(book: Book): String = book.bookUrl.replace("//", ".").replace(":", "").replace("/", ".")

  def props: Props = Props(new BookMetaActor())

  case class GetBookMeta(book: Book)

  case class BookPersisted(book: Book)

}

class BookMetaActor extends Actor with BookMetaUpdater with ConnectWithRetry {
  val log = Logging(context.system, this)

  override def receive: Actor.Receive = {
    case GetBookMeta(book) => {
      log.debug("Getting book meta")
      implicit val document: Document = connectWithRetry(book.bookUrl).get()
      val updatedBook = TwitterCountsFetcher.updateWithTwitterCount(getMeta(book))
      val authors = getAuthor(book)
      implicit val jdbcTemplate = Db.prodJdbcTemplateWithName(WotCrawlerApp.dbName)
      Db.insert.book(updatedBook)
      authors.map(Db.insert.author)
      sender() ! BookPersisted(book)
    }
  }
}