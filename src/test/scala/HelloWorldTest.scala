import org.scalatest.{Matchers, FreeSpec}

class HelloWorldTest extends FreeSpec with Matchers{

  "World" - {
    "should say hello" in {
      new HelloWorld().say should be("Hello World")
    }
  }
}