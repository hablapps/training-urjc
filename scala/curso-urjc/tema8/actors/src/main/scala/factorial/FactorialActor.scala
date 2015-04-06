package factorial

import scala.concurrent.duration._

import akka.actor.{ Actor, ActorRef, Props, PoisonPill }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout

class FactorialActor extends Actor {

  import context.dispatcher
  implicit val timeout = Timeout(5 seconds)

  var child: Option[ActorRef] = None

  def receive = {

    case Calculate(n: Int) if n < 2 =>
      sender ! Result(1)

    case Calculate(n: Int) =>
      (child.getOrElse(context.actorOf(Props[FactorialActor])) ? Calculate(n-1))
        .mapTo[Result]
        .map( childResult => Result(n * childResult.n))
        .pipeTo(sender)
  }
}

case class Calculate(n: Int)
case class Result(n: Int)
