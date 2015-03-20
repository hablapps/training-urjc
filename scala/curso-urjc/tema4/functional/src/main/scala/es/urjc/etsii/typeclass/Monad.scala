package es.urjc.etsii.typeclass

import scala.language.higherKinds

import es.urjc.etsii.adt._

trait Monad[M[_]] extends Functor[M] {

  def unit[A](value: A): M[A]

  def flatMap[A, B](m: M[A])(f: A => M[B]): M[B]

  def map[A, B](m: M[A])(f: A => B): M[B] = flatMap(m)(a => unit(f(a)))
}

object Monad {

  implicit val opcMonad = new Monad[Opcion] {

    def unit[A](value: A) = Algun(value)

    def flatMap[A, B](opc: Opcion[A])(f: A => Opcion[B]) = opc.flatMap(f)
  }

  implicit val lstMonad = new Monad[Lista] {

    def unit[A](value: A) = Cons(value, Nada)

    def flatMap[A, B](lst: Lista[A])(f: A => Lista[B]) = lst.flatMap(f)
  }
}
