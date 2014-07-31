package net.ikenna.wot.followersreaders

import net.ikenna.wot._
import net.ikenna.wot.Author2
import net.ikenna.wot.util.{WotJson, WotCsvWriter}
import ikenna.wot.BooksDb

/**
 * Correlation between the sum of author(s) twitter followers and the number of readers of their leanpub book
 */

object FollowersReadersCorrelationApp extends App with FollowersReadersCorrelation {

  run(books = BooksDb.allBooks, outputFileName = "followers-readers-correlation")
}

/**
 * For paid books only - Correlation between the sum of author(s) twitter followers and the number of readers of their leanpub book
 */
object FollowersReadersCorrelationPaidBooksApp extends App with FollowersReadersCorrelation {

  run(books = BooksDb.paidBooksOnly, outputFileName = "paid-books-followers-readers-correlation")
}

/**
 * For books with readers only - Correlation between the sum of author(s) twitter followers and the number of readers of their leanpub book
 */
object FollowersReadersCorrelationPaidReaderBooksApp extends App with FollowersReadersCorrelation {

  run(books = BooksDb.hasMaxPriceAndReaders, outputFileName = "readers-paid-books-correlation")
}

/**
 * For books with readers, and a max and min price -
 */
object ReadersMaxMinPriceCorrelationApp extends App with FollowersReadersCorrelation {

  run(books = BooksDb.readersMaxMinPrice, outputFileName = "readers-min-max-price-correlation")
}


trait FollowersReadersCorrelation extends WotLogger {


  implicit class AllAuthorsFollowers(b: Book2) {
    def sumOfAllAuthorsFollowers: Int = b.authors.map(a => a.followerCount.getOrElse(0)).sum

    def minPrice: Int = b.meta.price.map(p => p.min.getOrElse(-1)).getOrElse(-1)

    def maxPrice: Int = b.meta.price.map(p => p.max.getOrElse(-1)).getOrElse(-1)

    def readerCount: Int = b.meta.readers.map(r => r).getOrElse(0)

  }

  def run(books: Seq[Book2], outputFileName: String) = {

    defaultLogger.info("Started. Twitter Followers and Leanpub Readers correlation from Db + " + BooksDb.fileName)

    val authorsReaders: Seq[AuthorFollowersReaders] = books.map(b =>
      AuthorFollowersReaders(b.title, b.authors, b.sumOfAllAuthorsFollowers, b.readerCount, b.minPrice, b.maxPrice))

    val result: Result = Result(
      "Set of authors for each book, their total number of followers, and the number of readers for their book",
      authorsReaders)

    val output = WotJson.serializeToJsonFile(outputFileName, result)

    val headings = List("Book Title", "Sum of authors twitter followers", "Number of readers", "min price", "max price")

    def result2CsvLine(a: AuthorFollowersReaders): Seq[String] = List(a.bookTitle, a.followers.toString, a.readers.toString, a.minPrice.toString, a.maxPrice.toString)

    WotCsvWriter.writeToCsv2(result.authorsReaders, headings, result2CsvLine, outputFileName)

    defaultLogger.info("Finished. Result = " + output)
  }

  case class AuthorFollowersReaders(bookTitle: String, authors: Set[Author2], followers: Int, readers: Int, minPrice: Int, maxPrice: Int)

  case class Result(description: String, authorsReaders: Seq[AuthorFollowersReaders])

}

