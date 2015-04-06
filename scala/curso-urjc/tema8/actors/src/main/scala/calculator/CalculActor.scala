package calculator

import akka.actor.Actor

class CalculActor extends Actor {

  var accum = 0

  def receive = ready

  def ready: Receive = {
    case IWantToSum(n: Int) =>
      accum = n
      context.become(waiting)
  }

  def waiting: Receive = {
    case Plus(n: Int) =>
      println(s"Sum result: ${n + accum}")
      accum = 0
      context.become(ready)
  }

}

case class IWantToSum(n: Int)
case class Plus(n: Int)
