package calculator

import akka.actor.{ Props, ActorSystem }

object CalculatorBoot extends App {

  val system = ActorSystem("my-calculActor-system")
  val calculActor = system.actorOf(Props[CalculActor], "calculActor-actor")

  calculActor ! IWantToSum(1)
  calculActor ! Plus(3) // 4

  calculActor ! IWantToSum(100)
  calculActor ! Plus(1) //101

  system.shutdown()
}
