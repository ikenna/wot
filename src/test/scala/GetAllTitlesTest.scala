import org.scalatest.{ BeforeAndAfterAll, Matchers, FreeSpec }
import scala.collection.immutable.Seq
import scala.xml.NodeSeq

class GetAllTitlesTest extends FreeSpec with Matchers with BeforeAndAfterAll {

  "Features" - {
    "list all categories" in {
      println(CategoryLinks())
    }
  }

}

object CategoryLinks {

  val categories =
    <categories>
      <option value="academic">Academic</option>
      <option value="agile">Agile</option>
      <option value="bcwriters">BC Writers</option>
      <option value="biographies">Biographies</option>
      <option value="business">Business and Economics</option>
      <option value="childrensbooks">
        Children
        &#x27;
        s Books
      </option>
      <option value="cookbooks">Cookbooks</option>
      <option value="culture">Culture</option>
      <option value="diet">Diet and Nutrition</option>
      <option value="diy">DIY</option>
      <option value="erotica">Erotica</option>
      <option value="familyandparenting">Family and Parenting</option>
      <option value="fanfiction">Fan Fiction</option>
      <option value="fantasy">Fantasy</option>
      <option value="fiction">Fiction</option>
      <option value="general">General</option>
      <option value="historical_fiction">Historical Fiction</option>
      <option value="history">History</option>
      <option value="horror">Horror</option>
      <option value="humor">Humor</option>
      <option value="humorandsatire" selected="selected">Humor and Satire</option>
      <option value="internet">Internet</option>
      <option value="music">Music</option>
      <option value="mystery">Mystery</option>
      <option value="nanowrimo">NaNoWriMo</option>
      <option value="poetry">Poetry</option>
      <option value="religion">Religion</option>
      <option value="romance">Romance</option>
      <option value="science_fiction">Science Fiction</option>
      <option value="selfhelp">Self-Help</option>
      <option value="serialfiction">Serial Fiction</option>
      <option value="software">Software</option>
      <option value="sports">Sports and Fitness</option>
      <option value="startups">Startups</option>
      <option value="textbooks">Textbooks</option>
      <option value="thriller">Thriller</option>
      <option value="travel">Travel</option>
      <option value="young_adult">Young Adult</option>
    </categories>

  def apply(): Seq[CategoryLink] = categories \\ "option" map {
    o => CategoryLink("https://leanpub.com/c/" + o \\ "@value")
  }

}