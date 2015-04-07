package time

import akka.actor.{ Props, ActorSystem }

import scala.concurrent.duration._
import scala.language.postfixOps

object TimerBoot extends App {

  val system = ActorSystem("my-timer-system")

  val printerActor = system.actorOf(Props[PrinterActor], "printer-actor")
  
  val timerActor = system.actorOf(TimerActor.props(printerActor), "timer-actor")

  import system.dispatcher

  val timerServiceScheduler =
    system.scheduler.schedule(
      0 milliseconds,
      1 seconds,
      timerActor,
      GiveMeTime)

  Thread.sleep(3400)

  system.shutdown()
}