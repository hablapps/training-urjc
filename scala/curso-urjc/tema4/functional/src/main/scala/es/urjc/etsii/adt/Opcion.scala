package es.urjc.etsii.adt

import scala.language.implicitConversions

sealed trait Opcion[+A] {

  def isDefined = this != Ninguno

  def get: A = this match {
    case Algun(x) => x
    case _ => throw new Error("Ninguno.get")
  }

  def map[B](f: A => B): Opcion[B] = this match {
    case Ninguno => Ninguno
    case Algun(a) => Algun(f(a))
  }

  def bind[B](f: A => Opcion[B]): Opcion[B] = this match {
    case Ninguno => Ninguno
    case Algun(a) => f(a)
  }

  def flatMap[B](f: A => Opcion[B]): Opcion[B] = this match {
    case Ninguno => Ninguno
    case Algun(a) => f(a)
  }
}

case class Algun[A](valor: A) extends Opcion[A]

case object Ninguno extends Opcion[Nothing]

object Opcion {
  def apply[A](arg: A): Opcion[A] = Algun(arg)
}
