package factorial

import scala.concurrent.duration._

import akka.actor.{ Actor, ActorRef, Props, PoisonPill }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout

class FactorialActor extends Actor {

  import context.dispatcher
  implicit val timeout = Timeout(5 seconds)

  val child: ActorRef = context.actorOf(Props[FactorialActor])

  def receive = {

    case Calculate(n: Int) if n < 2 =>
      sender ! Result(1)
      child ! PoisonPill

    case Calculate(n: Int) =>
      (child ? Calculate(n-1)).mapTo[Result].map {
        case Result(result) => Result(n * result)
      }.pipeTo(sender)

      child ! PoisonPill
  }
}

case class Calculate(n: Int)
case class Result(n: Int)
