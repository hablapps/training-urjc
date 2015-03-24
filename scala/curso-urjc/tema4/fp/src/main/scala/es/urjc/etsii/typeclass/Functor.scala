package es.urjc.etsii.typeclass

import scala.language.higherKinds

import es.urjc.etsii.adt._

trait Functor[F[_]] {

  def map[A, B](value: F[A])(f: A => B): F[B]

  def distribute[A, B](arg: F[(A, B)]): (F[A], F[B]) =
    (map(arg)(tpl => tpl._1), map(arg)(tpl => tpl._2))

  def replace[A, B](arg: F[A], b: B): F[B] = map(arg)(_ => b)

  def strengthR[A, B](fa: F[A], b: B): F[(A, B)] = map(fa)(a => (a, b))
}

object Functor {

  implicit val lstFunctor: Functor[Lista] = new Functor[Lista] {
    def map[A, B](value: Lista[A])(f: A => B) = value.map(f)
  }

  implicit val opcFunctor: Functor[Opcion] = new Functor[Opcion] {
    def map[A, B](value: Opcion[A])(f: A => B) = value.map(f)
  }
}
