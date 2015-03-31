package adder

import akka.actor.{ Props, ActorSystem }

object Adderoot extends App {

  val system = ActorSystem("my-adder-system")
  val adderActor = system.actorOf(Props[AdderActor], "adder-actor")

  adderActor ! Add(1)
  adderActor ! PrintAcum //1

  adderActor ! Add(2)
  adderActor ! PrintAcum //3

  adderActor ! Add(-1)
  adderActor ! PrintAcum //2

  system.shutdown()
}