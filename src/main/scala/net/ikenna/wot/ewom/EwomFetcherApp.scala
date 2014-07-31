package net.ikenna.wot.ewom

import akka.actor.{ Actor, Props, ActorSystem }
import net.ikenna.wot.ewom.EwomMaster.FetchEwom
import net.ikenna.wot.{ Book3, Book2 }
import akka.routing.RoundRobinPool
import net.ikenna.wot.ewom.Worker.{ GetEwom, EwomResult }
import akka.event.Logging
import net.ikenna.wot.util.{ WotCsvWriter, WotJson }
import ikenna.wot.BooksDb

object EwomFetcherApp extends App {

  val allBooks = BooksDb.allBooks
  val actorSystem = ActorSystem("ewomfetcher")
  val ewomMaster = actorSystem.actorOf(Props(new EwomMaster(allBooks)))
  ewomMaster ! FetchEwom()
}

object EwomMaster {

  case class FetchEwom()

}

class EwomMaster(allBooks: Seq[Book2]) extends Actor {
  val akkaLogger = Logging(context.system, this)

  var results = Seq[Book3]()

  val numberOfWorkers = 5
  val workerRouter = context.system.actorOf(Props[Worker].withRouter(RoundRobinPool(5)), name = "WorkerRouter")

  override def receive: Actor.Receive = {
    case FetchEwom() => {
      akkaLogger.info("Started. Fetching ewom")
      for (book <- allBooks) {
        workerRouter ! GetEwom(book)
      }
    }

    case EwomResult(book) => {
      akkaLogger.info("Received EwomResult for " + book.bookUrl + " " + book.ewom)
      results = results :+ book
      val resultSize = results.size
      if (resultSize == allBooks.size) {
        akkaLogger.info("Finished fetching all ewom. Writing to file.")
        WotJson.serializeToJson(results)
        writeToCsv(results)
      } else {
        akkaLogger.info("Results not yet complete. Result size - " + resultSize + ". Total expected " + allBooks.size)
      }
    }

      def writeToCsv(books: Seq[Book3]): Unit = {
        akkaLogger.info("Writing to CSV")
        val headings = List("book url", "reader count", "twitter count", "facebook count", "total fb and twitter")
        def data2CsvLine = (b: Book3) => {
          val twitter = b.ewom.twitter.getOrElse(0)
          val facebook = b.ewom.facebook.getOrElse(0)
          val total = twitter + facebook
          val numReaders = b.meta.readers.getOrElse(0).toString
          List(b.bookUrl, numReaders, twitter.toString, facebook.toString, total.toString)
        }
        WotCsvWriter.writeToCsv2(books, headings, data2CsvLine, "books-with-ewom")
      }

  }
}

object Worker {

  case class EwomResult(books: Book3)

  case class GetEwom(book: Book2)

}

class Worker extends Actor with EwomFetcher {
  val akkaLogger = Logging(context.system, this)
  akkaLogger.info("Started Worker - " + this.toString)

  override def receive: Actor.Receive = {
    case GetEwom(book) => {
      akkaLogger.info("Getting ewom for " + book.bookUrl)
      val ewom = getEwom(book.bookUrl)
      val result = Book3(book.bookUrl, book.title, book.meta, ewom, book.authors)
      sender ! EwomResult(result)
    }
  }
}

