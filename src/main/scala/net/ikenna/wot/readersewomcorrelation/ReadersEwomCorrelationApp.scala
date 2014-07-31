package net.ikenna.wot.readersewomcorrelation

import net.ikenna.wot.{Book3, Author2, WotLogger}
import net.ikenna.wot.util.{WotCsvWriter, WotJson}
import ikenna.wot.BooksDb

object ReadersEwomCorrelationApp extends App with FollowersReadersCorrelation {

  run(books = BooksDb.readersMaxMinPrice3, outputFileName = "book3-readers-min-max-price-correlation")
}


trait FollowersReadersCorrelation extends WotLogger {

  import RichBook3Obj._



  def run(books: Seq[Book3], outputFileName: String) = {

    defaultLogger.info("Started. from Db + " + BooksDb.fileName)

    val authorsReaders: Seq[CsvInfo] = books.map(b =>
      CsvInfo(b.title, b.authors, b.sumOfAllAuthorsFollowers, b.readerCount, b.minPrice, b.maxPrice, b.twitterCount, b.facebookCount))

    val result: Result = Result(
      "Set of authors for each book, their total number of followers, and the number of readers for their book",
      authorsReaders)

    val output = WotJson.serializeToJsonFile(outputFileName, result)

    val headings = List("Book Title", "Sum of authors twitter followers", "Number of readers", "min price", "max price", "Twitter count", "facebook count")

    def result2CsvLine(a: CsvInfo): Seq[String] = List(a.bookTitle, a.followers.toString, a.readers.toString, a.minPrice.toString, a.maxPrice.toString, a.twitterCount.toString, a.facebookCount.toString)

    WotCsvWriter.writeToCsv2(result.authorsReaders, headings, result2CsvLine, outputFileName)

    defaultLogger.info("Finished. Result = " + output)
  }

  case class CsvInfo(bookTitle: String, authors: Set[Author2], followers: Int, readers: Int, minPrice: Int, maxPrice: Int, twitterCount:Int, facebookCount:Int)

  case class Result(description: String, authorsReaders: Seq[CsvInfo])

}

object RichBook3Obj{
  implicit class RichBook3(b: Book3) {
    def sumOfAllAuthorsFollowers: Int = b.authors.map(a => a.followerCount.getOrElse(0)).sum

    def minPrice: Int = b.meta.price.map(p => p.min.getOrElse(-1)).getOrElse(-1)

    def maxPrice: Int = b.meta.price.map(p => p.max.getOrElse(-1)).getOrElse(-1)

    def hasReaders: Boolean = b.meta.readers.map(_ > 0).getOrElse(false)

    def hasMaxPriceGreaterThanZero: Boolean = b.meta.price.map { p =>
      p.max.getOrElse(0) > 0
    }.getOrElse(false)

    def hasMinPriceGreaterThanZero: Boolean = b.meta.price.map { p =>
      p.min.getOrElse(0) > 0
    }.getOrElse(false)

    def readerCount: Int = b.meta.readers.map(r => r).getOrElse(0)

    def twitterCount:Int = b.ewom.twitter.getOrElse(0) / 100

    def facebookCount:Int = b.ewom.facebook.getOrElse(0) / 100
  }
}