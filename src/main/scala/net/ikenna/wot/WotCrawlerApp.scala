package net.ikenna.wot

import akka.actor.ActorSystem
import net.ikenna.wot.CategoryCrawler.Crawl

object WotCrawlerApp extends App with CreateDBName {
  val dbName = createDBName
  implicit val jdbcTemplate = Db.prodJdbcTemplateWithName(dbName)
  Db.loadSchema()
  Db.testConnection
  val system = ActorSystem("WotCrawler")
  val categoryCrawler = system.actorOf(CategoryCrawler.props, CategoryCrawler.name)
  categoryCrawler ! Crawl
}