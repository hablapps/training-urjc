package helloworld

import akka.actor.Actor

class MyFirstActor extends Actor {

  def receive = {
    case _ => println("Hello World!")
  }

}