package time

import akka.actor.Actor

class PrinterActor extends Actor {

  def receive = {
    case msg: String => println(msg)
  }
  
}
