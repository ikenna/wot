package net.ikenna.wot.twitternontwitter

import net.ikenna.wot.{ WotLogger, WotJson, Author2, Book2 }

object TwitterNonTwitterAuthorsQueryApp extends App with WotLogger {

  val fileName = "books-2014-07-19-23-17-18.json"
  defaultLogger.info("Started. Finding percentage of twitter vs non-twitter authors with Db + " + fileName)
  val books: Seq[Book2] = WotJson.deSerializeBooks(fileName)
  val totalAuthors: Set[Author2] = books.map(b => b.authors).flatten.toSet
  val twitterAuthors: Set[Author2] = totalAuthors.filter(a => a.twitterUrl.isDefined)
  val nonTwitterAuthors: Set[Author2] = totalAuthors.filterNot(a => a.twitterUrl.isDefined)
  val percentTwitterAuthors: Double = (twitterAuthors.size / totalAuthors.size.toDouble) * 100
  val result: Result = Result(totalAuthors.size, twitterAuthors.size, nonTwitterAuthors.size, percentTwitterAuthors)
  WotJson.serializeToJson(fileName = "percentage-twitter", result)

  defaultLogger.info("Finished. Result(totalAuthors, twitterAuthors, nonTwitterAuthors, percentTwitterAuthors) = " + result)
}

case class Result(totalAuthors: Int, twitterAuthors: Int, nonTwitterAuthors: Int, percentTwitterAuthors: Double)
