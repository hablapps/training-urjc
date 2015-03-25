package es.urjc.etsii.adt

sealed trait Opcion[+A] {

  def map[B](f: A => B): Opcion[B] = this match {
    case Ninguno => Ninguno
    case Algun(a) => Algun(f(a))
  }

  def flatMap[B](f: A => Opcion[B]): Opcion[B] = this match {
    case Ninguno => Ninguno
    case Algun(a) => f(a)
  }

  def isDefined: Boolean = this != Ninguno

  def get: A = this match {
    case Algun(a) => a
    case Ninguno => throw new Error("Ninguno.get")
  }
}

case class Algun[A](valor: A) extends Opcion[A]

case object Ninguno extends Opcion[Nothing]
