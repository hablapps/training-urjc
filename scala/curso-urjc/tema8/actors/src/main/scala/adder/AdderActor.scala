package adder

import akka.actor.Actor

class AdderActor extends Actor {

  var acum = 0

  def receive = {
    case Add(n: Int) => acum = acum + n
    case PrintAcum => println(s"Acum: $acum")
  }

}

case class Add(n: Int)
case object PrintAcum
