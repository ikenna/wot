package net.ikenna.wot.groupby

import net.ikenna.wot.{ Book3, WotLogger }
import net.ikenna.wot.util.{WotCsvWriter, WotJson}
import net.ikenna.wot.followersreaders.BooksDb
import net.ikenna.wot.readersewomcorrelation.RichBook3Obj

object GroupByReadersApp extends App with WotLogger {
  defaultLogger.info("Started. ")
  val books: Seq[Book3] = BooksDb.allBooks3

  import Categories._
  import RichBook3Obj._

  val zero: Zero = Zero(books.filter { b => b.readerCount == 0 })
  val oneToNine: OneTo9 = OneTo9(books.filter { b => 1 <= b.readerCount && b.readerCount <= 9 })
  val tenToHundred: TenTo99 = TenTo99(books.filter { b => (10 <= b.readerCount && b.readerCount <= 99) })
  val hundredTo999: HundredTo999 = HundredTo999(books.filter { b => (100 <= b.readerCount && b.readerCount <= 999) })
  val oneThousandTo9999: OneThousandTo9999 = OneThousandTo9999(books.filter { b => (1000 <= b.readerCount && b.readerCount <= 9999) })
  val tenThousandTo99999: TenThousandTo99999 = TenThousandTo99999(books.filter { b => (10000 <= b.readerCount && b.readerCount <= 99999) })
  val hundredThousandTo999999: HundredThousandTo999999 = HundredThousandTo999999(books.filter { b => (100000 <= b.readerCount && b.readerCount <= 999999) })

  val result = List(zero.size.toString, oneToNine.size.toString, tenToHundred.size.toString, hundredTo999.size.toString, oneThousandTo9999.size.toString, tenThousandTo99999.size.toString, hundredThousandTo999999.size.toString)

  val headings = List("0", "1 to 9", "10 to 99", "100 to 999", "1000 to 9999", "10000 to 99999", "100000 to 999999")
  val lines:List[List[String]] = List(headings, result)
  WotCsvWriter.writeToCsv(lines, "group-by-readers")

  defaultLogger.info("Finished. Result  = " + result)
}

object Categories {

  trait HasSize {
    val books: Seq[Book3]

    def size = books.size
  }

  case class Zero(books: Seq[Book3]) extends HasSize

  case class OneTo9(books: Seq[Book3]) extends HasSize

  case class TenTo99(books: Seq[Book3]) extends HasSize

  case class HundredTo999(books: Seq[Book3]) extends HasSize

  case class OneThousandTo9999(books: Seq[Book3]) extends HasSize

  case class TenThousandTo99999(books: Seq[Book3]) extends HasSize

  case class HundredThousandTo999999(books: Seq[Book3]) extends HasSize

  case class Result(zero:Int, zeroTo9: Int, TenTo99: Int, hundredTo999: Int, oneThousandTo9999: Int,
    tenThousandTo99999: Int, hundredThousandTo999999: Int)

}

