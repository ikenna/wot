package net.ikenna.wot.groupby

import net.ikenna.wot.{ Book2, WotLogger }
import net.ikenna.wot.util.WotJson

object GroupByReadersApp extends App with WotLogger {
  val fileName = "db/books-2014-07-19-23-17-18.json"
  defaultLogger.info("Started. Reader categories from Db + " + fileName)
  val books: Set[Book2] = WotJson.deSerializeBooks(fileName).toSet

  import Categories._

  implicit class BookReaderCount(b: Book2) {
    def readerCount: Int = b.meta.readers.map(r => r).getOrElse(0)
  }

  val zeroToTen: ZeroTo9 = ZeroTo9(books.filter { b => 0 <= b.readerCount && b.readerCount <= 9 })
  val tenToHundred: TenTo99 = TenTo99(books.filter { b => (10 <= b.readerCount && b.readerCount <= 99) })
  val hundredTo999: HundredTo999 = HundredTo999(books.filter { b => (100 <= b.readerCount && b.readerCount <= 999) })
  val oneThousandTo9999: OneThousandTo9999 = OneThousandTo9999(books.filter { b => (1000 <= b.readerCount && b.readerCount <= 9999) })
  val tenThousandTo99999: TenThousandTo99999 = TenThousandTo99999(books.filter { b => (10000 <= b.readerCount && b.readerCount <= 99999) })
  val hundredThousandTo999999: HundredThousandTo999999 = HundredThousandTo999999(books.filter { b => (100000 <= b.readerCount && b.readerCount <= 999999) })

  val result = Result(zeroToTen.size, tenToHundred.size, hundredTo999.size, oneThousandTo9999.size, tenThousandTo99999.size, hundredThousandTo999999.size)

  WotJson.serializeToJsonFile(fileName = "reader-categories", result)

  defaultLogger.info("Finished. Result(zeroTo9 , TenTo99 , hundredTo999 , oneThousandTo9999 , tenThousandTo99999 , hundredThousandTo999999 ) = " + result)
}

object Categories {

  trait HasSize {
    val books: Set[Book2]

    def size = books.size
  }

  case class ZeroTo9(books: Set[Book2]) extends HasSize

  case class TenTo99(books: Set[Book2]) extends HasSize

  case class HundredTo999(books: Set[Book2]) extends HasSize

  case class OneThousandTo9999(books: Set[Book2]) extends HasSize

  case class TenThousandTo99999(books: Set[Book2]) extends HasSize

  case class HundredThousandTo999999(books: Set[Book2]) extends HasSize

  case class Result(zeroTo9: Int, TenTo99: Int, hundredTo999: Int, oneThousandTo9999: Int,
    tenThousandTo99999: Int, hundredThousandTo999999: Int)

}

