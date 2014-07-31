package ikenna.wot

import net.ikenna.wot.{Price, Book3, Book2}
import net.ikenna.wot.util.WotJson

object BooksDb {

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
