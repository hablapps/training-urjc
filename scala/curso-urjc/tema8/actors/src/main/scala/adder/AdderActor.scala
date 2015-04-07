package adder

import akka.actor.Actor

class AdderActor extends Actor {

  var accum = 0

  def receive = {
    case Add(n: Int) => accum = accum + n
    case PrintAccum => println(s"Accum: $accum")
  }

}

case class Add(n: Int)
case object PrintAccum
