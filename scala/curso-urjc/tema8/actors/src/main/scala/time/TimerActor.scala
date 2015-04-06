package time

import akka.actor.{ Actor, ActorRef, Props }
import java.util.Date

class TimerActor(printlnActor: ActorRef) extends Actor {

  def receive = {
    case GiveMeTime => 
      printlnActor ! new Date().toString
  }

}

object TimerActor {
  def props(printlnActor: ActorRef): Props = Props(new TimerActor(printlnActor))
}

case object GiveMeTime
