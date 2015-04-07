package adder

import akka.actor.{ Props, ActorSystem }

object Adderoot extends App {

  val system = ActorSystem("my-adder-system")
  val adderActor = system.actorOf(Props[AdderActor], "adder-actor")

  adderActor ! Add(1)
  adderActor ! PrintAccum //1

  adderActor ! Add(2)
  adderActor ! PrintAccum //3

  adderActor ! Add(-1)
  adderActor ! PrintAccum //2

  system.shutdown()
}