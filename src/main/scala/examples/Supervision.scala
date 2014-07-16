package examples

import akka.actor._
import examples.Child.{ ChildException, ThrowException, Tick }
import scala.concurrent.duration._
import akka.actor.Terminated
import examples.Supervisor.Supervise
import akka.actor.SupervisorStrategy.{ Restart, Resume, Stop }

object Supervision extends App {

  val system = ActorSystem("Supervision")
  val supervisor = system.actorOf(Supervisor.props, Supervisor.name)
  supervisor ! Supervise

}

object Supervisor {

  def props: Props = Props(new Supervisor)

  def name = "Supervisor"

  case class Supervise()

}

class Supervisor extends Actor {

  import context.dispatcher

  override def supervisorStrategy = OneForOneStrategy() {
    case _: ChildException => {
      println("Supervisor notified of exception ** ")
      Restart
    }
  }

  override def receive: Actor.Receive = {
    case Supervise => {
      val child = context.actorOf(Child.props, Child.name)
      context.system.scheduler.schedule(0 second, 200 millis)(child ! Tick)
      context.watch(child)
      context.system.scheduler.scheduleOnce(2 second)(child ! ThrowException)
      context.system.scheduler.scheduleOnce(5 second)(context.system.shutdown())
    }

    case Terminated(watched) => {
      println("Terminated " + watched)
    }

  }
}

object Child {
  def props = Props(new Child)

  def name = "child"

  case class ThrowException()

  case class Tick()

  case class ChildException() extends RuntimeException

}

class Child extends Actor {

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("Restarting child ****")
  }

  override def receive: Actor.Receive = {

    case ThrowException => {
      throw new ChildException()
    }

    case Tick => {
      println("Child is running")
    }

  }
}