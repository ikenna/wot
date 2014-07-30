package net.ikenna.wot.followersreaders

import net.ikenna.wot._
import net.ikenna.wot.Author2
import net.ikenna.wot.util.{WotJson, WotCsvWriter}

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

object BooksDb {

  //  val fileName = "db/books-2014-07-19-23-17-18.json"
  val fileName = "db/books-2014-07-21-10-20-31.json"

  val allBooks: Seq[Book2] = WotJson.deSerializeBooks(fileName)

  val allBooks3: Seq[Book3] = WotJson.deSerializeBooks3(fileName)

  def paidBooksOnly: Seq[Book2] = allBooks.filter(b => b.meta.price.isDefined)

  def hasMaxPriceAndReaders: Seq[Book2] = allBooks.filter { b =>
    b.meta.price.map(p => p.hasMaxPriceGreaterThanZero).getOrElse(false) &&
      b.meta.readers.map(r => r > 0).getOrElse(false)
  }

  def readersMaxMinPrice: Seq[Book2] = allBooks.filter(b => b.hasReaders && b.hasMaxPriceGreaterThanZero && b.hasMinPriceGreaterThanZero)

  def readersMaxMinPrice3: Seq[Book3] = {
    import net.ikenna.wot.readersewomcorrelation.RichBook3Obj._
    val allBooks3 = WotJson.deSerializeBooks3(fileName)
    allBooks3.filter(b => b.hasReaders && b.hasMaxPriceGreaterThanZero && b.hasMinPriceGreaterThanZero)
  }

  implicit class PriceInfo(p: Price) {
    def hasMaxPriceGreaterThanZero: Boolean = p.max.map(_ > 0).getOrElse(false)
  }

  implicit class BookInfo(b: Book2) {
    def hasReaders: Boolean = b.meta.readers.map(_ > 0).getOrElse(false)

    def hasMaxPriceGreaterThanZero: Boolean = b.meta.price.map { p =>
      p.max.getOrElse(0) > 0
    }.getOrElse(false)

    def hasMinPriceGreaterThanZero: Boolean = b.meta.price.map { p =>
      p.min.getOrElse(0) > 0
    }.getOrElse(false)
  }

}