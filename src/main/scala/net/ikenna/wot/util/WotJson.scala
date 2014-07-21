package net.ikenna.wot.util

import net.ikenna.wot.{Book2, RunTimeStamp, WotLogger}
import java.io.{File, PrintWriter}
import scala.collection.immutable.Iterable
import scala.io.Source
import net.ikenna.wot.readersauthor.BookFollower
import net.ikenna.wot.AuthorReaders

object WotJson extends WotLogger {

  import org.json4s._
  import org.json4s.jackson.Serialization
  import org.json4s.jackson.Serialization._

  implicit val formats = Serialization.formats(NoTypeHints)

  def serializeToJsonFile(fileName: String, output: AnyRef):String = {
    val file = "results/" + fileName + "-" + RunTimeStamp() + ".json"
    val ser = writePretty(output)
    val writer = new PrintWriter(file)
    writer.println(ser)
    writer.close()
    ser
  }

  def serializeToJson(bookFollower: Seq[BookFollower]): Unit = {
    val fileName = "book-follower-" + RunTimeStamp() + ".json"
    val ser = write(bookFollower)
    val writer = new PrintWriter(fileName);
    writer.println(ser)
    writer.close()
  }

  def serializeToJson(authorReaders: Iterable[AuthorReaders]): Unit = {
    val fileName = "author-reader-" + RunTimeStamp() + ".json"
    val ser = write(authorReaders)
    val writer = new PrintWriter(fileName, "UTF-8");
    writer.println(ser)
    writer.close()
  }

  def serializeToJson(books: Set[Book2]): Unit = {
    val fileName = "books-" + RunTimeStamp() + ".json"

    val ser = write(books)
    val writer = new PrintWriter(fileName, "UTF-8");
    writer.println(ser)
    writer.close()
  }

  def deSerializeBooks(fileName: String): List[Book2] = {
    val jsonFile = Source.fromFile(new File(fileName)).mkString
    assert(new File(fileName).exists())
    read[List[Book2]](jsonFile)
  }

}
