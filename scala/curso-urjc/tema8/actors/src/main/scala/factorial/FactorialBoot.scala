package factorial

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor.{ Props, ActorSystem, PoisonPill }
import akka.util.Timeout
import akka.pattern.ask

object FactorialBoot extends App {

  val system = ActorSystem("factorial-system")
  val factorialActor = system.actorOf(Props[FactorialActor], "factorial-actor")

  import system.dispatcher
  val duration = 5 seconds
  implicit val timeout = Timeout(duration)

  val future = (factorialActor ? Calculate(5)).mapTo[Result]

  val result = Await.result(future, duration)

  println(s"The result is: ${result.n}") //120

  factorialActor ! PoisonPill
  system.shutdown()

}
