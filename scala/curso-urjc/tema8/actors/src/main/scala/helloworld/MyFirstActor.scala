package helloworld

import akka.actor.Actor

class MyFirstActor extends Actor {

  val message = "Hello World!"

  def receive = {
    case _ => println(message)
  }

}
