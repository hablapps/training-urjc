package es.urjc.etsii.adt

sealed trait Cualquiera[I, +D] {

  def derecha: D = this match {
    case Derecha(d) => d
    case _ => throw new Error("Izquierda.derecha")
  }

  def isDerecha: Boolean = this match {
    case Derecha(_) => true
    case _ => false
  }

  def map[E](f: D => E): Cualquiera[I, E] = this match {
    case Derecha(d) => Derecha[I, E](f(d))
    case Izquierda(i) => Izquierda[I, E](i)
  }

  def flatMap[E](f: D => Cualquiera[I, E]): Cualquiera[I, E] = this match {
    case Derecha(d) => f(d)
    case Izquierda(i) => Izquierda[I, E](i)
  }
}

case class Izquierda[I, D](izqa: I) extends Cualquiera[I, D]

case class Derecha[I, D](dcha: D) extends Cualquiera[I, D]

object Cualquiera {

  def apply[I, D](value: D): Cualquiera[I, D] = Derecha[I, D](value)

  implicit class MapExtend[A, B](map: Map[A, B]) {
    def getCualquiera(key: A): Cualquiera[String, B] = map.get(key) match {
      case None => Izquierda(s"key '$key' was not found in map")
      case Some(value) => Derecha(value)
    }
  }
}
