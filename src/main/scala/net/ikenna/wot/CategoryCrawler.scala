package net.ikenna.wot

import akka.actor._
import akka.event.Logging
import scala.concurrent.duration._
import net.ikenna.wot.BookActor.GetBookData

object CategoryCrawler {

  case class Crawl()

  def props: Props = Props(new CategoryCrawler)

  def name: String = "category"
}

class CategoryCrawler extends Actor with GetTitleAndUrlFromCategory {

  import CategoryCrawler._
  import context.dispatcher

  val log = Logging(context.system, this)
  val children = collection.mutable.Set[ActorRef]()

  override def preStart = {
    shutdownSystemWhenChildrenFinish
  }
  override def receive: Actor.Receive = {
    case Crawl => bookActors.map(_ ! GetBookData())
    case Terminated(child) => onChildTermination(child)
  }

  def bookActors: Seq[ActorRef] = {
    log.info("Received message " + Crawl)
    getBookUrlAndTitleFrom(Categories.list).map {
      book =>
        val bookActor = context.actorOf(BookActor.props(book), BookActor.name(book))
        context.watch(bookActor)
        children.add(bookActor)
        bookActor
    }.toSeq
  }

  def onChildTermination(actorRef: ActorRef): Unit = {
    children.remove(actorRef)
    log.info(s"Actor ${actorRef.path} stopped. Remaining ${children.size} actors")
  }

  def shutdownSystemWhenChildrenFinish = {
    context.system.scheduler.schedule(10 seconds, 2 seconds) {
      if (children.isEmpty) {
        context.system.shutdown()
      } else {
        log.info("Children not finished yet")
      }
    }
  }
}