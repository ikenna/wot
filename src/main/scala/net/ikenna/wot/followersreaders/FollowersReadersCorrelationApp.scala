package net.ikenna.wot.followersreaders

import net.ikenna.wot._
import net.ikenna.wot.Author2

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

trait FollowersReadersCorrelation extends WotLogger {

  import net.ikenna.wot.groupby.GroupByReadersApp.BookReaderCount

  implicit class AllAuthorsFollowers(b: Book2) {
    def sumOfAllAuthorsFollowers: Int = b.authors.map(a => a.followerCount.getOrElse(0)).sum
  }

  def run(books: Seq[Book2], outputFileName: String) = {

    defaultLogger.info("Started. Twitter Followers and Leanpub Readers correlation from Db + " + BooksDb.fileName)

    val authorsReaders: Seq[AuthorFollowersReaders] = books.map(b =>
      AuthorFollowersReaders(b.title, b.authors, b.sumOfAllAuthorsFollowers, b.readerCount))

    val result: Result = Result(
      "Set of authors for each book, their total number of followers, and the number of readers for their book",
      authorsReaders)

    val output = WotJson.serializeToJsonFile(outputFileName, result)

    val headings = List("Book Title", "Sum of authors twitter followers", "Number of readers")

    def result2CsvLine(a: AuthorFollowersReaders): Seq[String] = List(a.bookTitle, a.followers.toString, a.readers.toString)

    WotCsvWriter.writeToCsv2(result.authorsReaders, headings, result2CsvLine, outputFileName)

    defaultLogger.info("Finished. Result = " + output)
  }

  case class AuthorFollowersReaders(bookTitle: String, authors: Set[Author2], followers: Int, readers: Int)

  case class Result(description: String, authorsReaders: Seq[AuthorFollowersReaders])

}

object BooksDb {

  val fileName = "db/books-2014-07-19-23-17-18.json"

  val allBooks: Seq[Book2] = WotJson.deSerializeBooks(fileName)

  def paidBooksOnly: Seq[Book2] = allBooks.filter(b => b.meta.price.isDefined)

}