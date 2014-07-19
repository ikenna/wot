package net.ikenna.wot

import akka.actor.ActorSystem
import net.ikenna.wot.CategoryCrawler.{ Tick, Crawl }
import scala.concurrent.duration._

object WotCrawlerApp extends App with CreateDBName {
  val dbName = createDBName
  implicit val jdbcTemplate = Db.prodJdbcTemplateWithName(dbName)
  //  Db.loadSchema()
  //  Db.testConnection
  val system = ActorSystem("WotCrawler")
  val categoryCrawler = system.actorOf(CategoryCrawler.props, CategoryCrawler.name)
  categoryCrawler ! Crawl
  import system.dispatcher
  system.scheduler.schedule(10 seconds, 2 seconds)(categoryCrawler ! Tick())
}