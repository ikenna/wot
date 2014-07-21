package net.ikenna.wot.readersauthor

import net.ikenna.wot._
import net.ikenna.wot.authorfollower.TwitterAuthorSearch
import java.io.File
import com.github.tototoshi.csv.CSVReader
import net.ikenna.wot.util.{WotJson, WotCsvWriter}

object NumberOfReadersPerAuthorApp extends App with WotLogger {
  import NumberOfReadersPerAuthor._

  defaultLogger.info("Started NumberOfReadersPerAuthorApp")
  val authorFollowers = toAuthorFollowers(loadAuthorsFromFile("data.csv"))
  printOut(forEachBookSumFollowers(loadBooks, authorFollowers))
}

object NumberOfReadersPerAuthor extends TwitterAuthorSearch with WotLogger {
  import WotJson._
  import WotCsvWriter._
  def printOut(bookFollowers: List[BookFollower]) = {
    defaultLogger.info("Printing book followers")
    serializeToJson(bookFollowers)
    val lines: List[List[String]] = bookFollowers.map(bf =>
      List(bf.book.bookUrl, bf.followerCount.toString, bf.book.meta.readers.getOrElse(0).toString, bf.book.authors.toString))
    //    val header = List("book url", "num authors ", "follower count")
    //    val all:List[List[String]] = lines.:::(header)

    writeToCsv(lines, "bookfollowers")
  }
  def forEachBookSumFollowers(books: List[Book2], map: AuthorUrlFollower): List[BookFollower] = {
    defaultLogger.info("Summing followers for books")
    map.map(println)
    books.map { book =>
      defaultLogger.info("Book " + book.toString)
      BookFollower(book, getFollowerCountForAllAuthors(book, map))
    }
  }

  def getFollowerCountForAllAuthors(book: Book2, map: NumberOfReadersPerAuthor.AuthorUrlFollower): Int = {
    var followerCount = 0
    //    for (authorUrl <- book.authorUrls) {
    //
    //      val count = map.get(authorUrl.trim).getOrElse(0)
    //      defaultLogger.info("Count is  " + count)
    //
    //      followerCount = followerCount + count
    //    }
    //    defaultLogger.info("Total count is  " + followerCount)

    //    defaultLogger.info("Book = %s . Authors = %s . Total followers = %s .".format(book.bookUrl, book.authorUrls, followerCount))
    followerCount
  }

  def loadBooks(): List[Book2] = {
    defaultLogger.info("Loading books")
    WotJson.deSerializeBooks("books-2014-07-19-15-09-39.json")
  }

  def toAuthorFollowers(authorFollower: List[List[String]]): AuthorUrlFollower = {
    val result = authorFollower.map {
      a =>
        val authorUrl: String = a(0)
        val followerCount: Int = a(2).toInt
        (authorUrl.trim -> followerCount)
    }.toMap
    defaultLogger.info("To author followers e.g " + result.head)
    result
  }

  def loadAuthorsFromFile(fileName: String): List[List[String]] = {
    val reader = CSVReader.open(new File(fileName))
    val data: List[List[String]] = reader.all()
    reader.close()
    defaultLogger.info("Loaded authors from file. Size = " + data.size)
    data
  }

  type AuthorUrlFollower = Map[String, Int]

}

case class AuthorBook(author: Author, books: Set[Book])

case class AuthorReaders(author: Author, readers: Int)

object Data {

}

case class BookFollower(book: Book2, followerCount: Int)
