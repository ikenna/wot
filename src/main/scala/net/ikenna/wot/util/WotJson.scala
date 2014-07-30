package net.ikenna.wot.util

import net.ikenna.wot._
import java.io.{ File, PrintWriter }
import scala.collection.immutable.Iterable
import scala.io.Source
import net.ikenna.wot.readersauthor.BookFollower
import net.ikenna.wot.readersauthor.BookFollower
import net.ikenna.wot.AuthorReaders

object WotJson extends WotLogger {

  import org.json4s._
  import org.json4s.jackson.Serialization
  import org.json4s.jackson.Serialization._

  implicit val formats = Serialization.formats(NoTypeHints)

  def serializeToJsonFile(fileName: String, output: AnyRef): String = {
    val file = "results/" + fileName + "-" + RunTimeStamp() + ".json"
    val ser = writePretty(output)
    val writer = new PrintWriter(file)
    writer.println(ser)
    writer.close()
    ser
  }
  //
  //  def serializeToJson(bookFollower: Seq[BookFollower]): Unit = {
  //    val fileName = "book-follower-" + RunTimeStamp() + ".json"
  //    val ser = write(bookFollower)
  //    val writer = new PrintWriter(fileName);
  //    writer.println(ser)
  //    writer.close()
  //  }

  def serializeToJson(authorReaders: Iterable[AuthorReaders]): Unit = {
    val fileName = "author-reader-" + RunTimeStamp() + ".json"
    val ser = write(authorReaders)
    val writer = new PrintWriter(fileName, "UTF-8");
    writer.println(ser)
    writer.close()
  }

  def serializeToJson(books: Set[Book2]): Unit = {
    val fileName = "db/new/books-" + RunTimeStamp() + ".json"
    val ser = writePretty(books)
    val writer = new PrintWriter(fileName);
    writer.println(ser)
    writer.close()
  }

  def serializeToJson[T <: AnyRef](books: Seq[T]): Unit = {
    val fileName = "db/new/books-" + RunTimeStamp() + ".json"
    val ser = writePretty(books)
    val writer = new PrintWriter(fileName);
    writer.println(ser)
    writer.close()
  }

  def deSerializeBooks(fileName: String): List[Book2] = {
    val jsonFile = Source.fromFile(new File(fileName)).mkString
    assert(new File(fileName).exists())
    read[List[Book2]](jsonFile)
  }

  def deSerializeBooks3(fileName: String): List[Book3] = {
    val jsonFile = Source.fromFile(new File(fileName)).mkString
    assert(new File(fileName).exists())
    read[List[Book3]](jsonFile)
  }
}
