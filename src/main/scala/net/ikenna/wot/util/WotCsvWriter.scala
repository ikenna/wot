package net.ikenna.wot.util

import net.ikenna.wot.{ Book2, RunTimeStamp }
import com.github.tototoshi.csv.CSVWriter

object WotCsvWriter {
  def writeToCsv2[T <: Any](data: Seq[T], headings: Seq[String], data2CsvLine: (T) => Seq[String], file: String) = {
    val fileName = "results/" + file + RunTimeStamp() + ".csv"
    val writer = CSVWriter.open(fileName, append = true)
    writer.writeRow(headings)
    data.map(dataLine => writer.writeRow(data2CsvLine(dataLine)))
    writer.close()
  }

  def writeToCsv(lines: List[List[Any]], file: String) = {
    val fileName = file + RunTimeStamp() + ".csv"
    val writer = CSVWriter.open(fileName, append = true)
    writer.writeAll(lines)
    writer.close()
  }

  def writeBooksToCsv(book: Set[Book2]) = {
    val fileName = "books-" + RunTimeStamp() + ".csv"
    val all: List[List[String]] = book.toList.map(getCsvLine)
    val writer = CSVWriter.open(fileName, append = true)
    writer.writeAll(all)
    writer.close()
  }

  def getCsvLine(book: Book2): List[String] = {
    List(book.bookUrl,
      book.meta.readers.getOrElse(0).toString,
      Book2.sumOfAllAuthorsFollowers(book),
      book.meta.language.getOrElse("")
    )
  }
}
