package net.ikenna.wot.twitter

import net.ikenna.wot.Db

object MyTest extends App{

  implicit val template = Db.prodJdbcTemplateWithName("prod-wotdb-TueJul15181608BST2014.mv.db")
  val result = Db.get.books.filter(_.numberOfTweets.fold(false)(_ >= 1))
  println(result.head)
}
