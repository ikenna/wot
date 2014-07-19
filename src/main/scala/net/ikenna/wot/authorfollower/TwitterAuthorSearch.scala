package net.ikenna.wot.authorfollower

import net.ikenna.wot.{ ConnectWithRetry, Db, Author }
import java.util.logging.Logger
import scala.util.{ Failure, Success, Try }
import akka.event.{ NoLogging, LoggingAdapter }
import java.io.File
import com.github.tototoshi.csv.CSVWriter

object TwitterAuthorSearchApp extends App with TwitterAuthorSearch {
  writeToCSV(fetchFollowerCount(unique(removeNoneTwitterAuthors(loadAuthorsFromDb))))
}

trait TwitterAuthorSearch {
  val logging = Logger.getLogger(this.getClass.getName)

  val dbName = "july14-sentiment"
  implicit val template = Db.prodJdbcTemplateWithName(dbName)

  def loadAuthorsFromDb: Seq[Author] = {
    val result = Db.get.authors(template)
    logging.info("Number of author-books loaded from DB  %s ".format(result.size))
    result
  }

  def removeNoneTwitterAuthors(authors: Seq[Author]): Seq[Author] = {
    val result = authors.filter(a => a.twitterUrl != None)
    logging.info("Removed non twitter authors. Twitter authors = %s".format(result.size))
    result
  }

  def unique(authors: Seq[Author]): Seq[Author] = {
    val result = authors.map(a => (a.twitterUrl.get.trim, a)).toMap.values.toList
    logging.info("Number of unique authors = %s".format(result.size))
    result
  }

  def fetchFollowerCount(authors: Seq[Author]): Seq[AuthorFollowers] = authors.map(a => AuthorFollowers(a, TwitterAuthorFollowers.getAuthorFollowerCount(a)))

  case class AuthorFollowers(author: Author, followersCount: Option[String])

  def writeToCSV(followers: Seq[AuthorFollowers]): Unit = {
    val f = new File("author-followers-2.csv")
    val writer = CSVWriter.open(f)
    writer.writeRow(List("Leanpub author url", "Twitter Url", "Follower count"))
    followers.map(a => writer.writeRow(List(a.author.authorUrl, a.author.twitterUrl.getOrElse("[Not twitter url]"), a.followersCount.getOrElse("[Error - check manually]"))))
    writer.close()
  }

}

object TwitterAuthorFollowers extends ConnectWithRetry {
  val logging = Logger.getLogger(this.getClass.getName)

  def getAuthorFollowerCount(author: Author): Option[String] = {
    Try(getCount(author)) match {
      case Success(count) => {
        logging.info("Followers for  %s = %s".format(author.twitterUrl, count))
        count
      }
      case Failure(e) => {
        logging.warning("Error getting follower count for %s %s %s".format(author.authorUrl, author.twitterUrl, e.getMessage))
        None
      }
    }
  }

  private def getCount(author: Author): Option[String] = {
    author.twitterUrl.map(twitterUrl => getCountText(twitterUrl))
  }

  def getTwitterUrl(author: Author): Option[String] = {
    Option(connectWithRetry(author.authorUrl).get.select("#user_title > small:nth-child(2) > a").attr("href"))
  }

  def getCountText(twitterUrl: String): String = {
    connectWithRetry(twitterUrl).get().select("#page-container > div.ProfileCanopy.ProfileCanopy--withNav > div > div.ProfileCanopy-navBar > div > div > div.Grid-cell.u-size3of4 > div > div > ul > li.ProfileNav-item.ProfileNav-item--followers > a > span.ProfileNav-value").text()
  }

  override val log: LoggingAdapter = NoLogging
}