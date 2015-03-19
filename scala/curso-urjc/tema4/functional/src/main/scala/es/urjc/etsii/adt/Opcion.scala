package es.urjc.etsii.adt

sealed trait Opcion[+A] {

  def map[B](f: A => B): Opcion[B] = this match {
    case Ninguno => Ninguno
    case Algun(a) => Algun(f(a))
  }
}

case class Algun[A](valor: A) extends Opcion[A]

case object Ninguno extends Opcion[Nothing]

object Opcion {

  def apply[A](arg: A): Opcion[A] = Algun(arg)
}
