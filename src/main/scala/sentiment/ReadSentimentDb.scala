package sentiment

import net.ikenna.wot.Db
import net.ikenna.wot.util.WotJson

object ReadSentimentDb extends App{

  val dbName = "july14-sentiment"
  implicit val template = Db.prodJdbcTemplateWithName(dbName)
  val bookTweets = Db.get.bookTweets
  WotJson.serializeToJson(bookTweets, "bookTweets")




}
