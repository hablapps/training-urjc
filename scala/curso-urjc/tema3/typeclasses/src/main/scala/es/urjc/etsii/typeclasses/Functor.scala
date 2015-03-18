package es.urjc.etsii.typeclasses

import scala.language.higherKinds

trait Functor[F[_]] {
  def map[A, B](f: A => B)(value: F[A]): F[B]
}

object Functor {

  val optFunctor = new Functor[Option] {
    def map[A, B](f: A => B)(value: Option[A]): Option[B] = value match {
      case None => None
      case Some(x) => Some(f(x))
    }
  }

  val listFunctor = new Functor[List] {
    def map[A, B](f: A => B)(value: List[A]): List[B] = value match {
      case Nil => Nil
      case x :: xs => f(x) :: map(f)(xs)
    }
  }
}
