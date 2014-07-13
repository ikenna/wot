package net.ikenna.wot

import akka.actor.{ Actor, Props }
import akka.event.Logging
import net.ikenna.wot.BookMetaActor.{ BookPersisted, GetBookMeta }

object BookMetaActor {
  def name(book: Book): String = book.bookUrl.replace("//", ".").replace(":", "").replace("/", ".")

  def props: Props = Props(new BookMetaActor())

  case class GetBookMeta(book: Book)

  case class BookPersisted(book: Book)

}

class BookMetaActor extends Actor with BookMetaUpdater {
  val log = Logging(context.system, this)

  override def receive: Actor.Receive = {
    case GetBookMeta(book) => {
      log.debug("Getting book meta")
      val updatedBook = update(book)
      implicit val jdbcTemplate = Db.prodJdbcTemplateWithName(WotCrawlerApp.dbName)
      Db.insert.book(updatedBook)
      sender() ! BookPersisted(book)
    }
  }
}