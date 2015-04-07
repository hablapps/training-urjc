package helloworld

import akka.actor.{ Props, ActorSystem }

object HelloWorldBoot extends App {

  val system = ActorSystem("my-frist-system")
  val myActor = system.actorOf(Props[MyFirstActor], "my-first-actor")

  myActor ! "Start!"

  system.shutdown()
}