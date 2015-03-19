package es.urjc.etsii.typeclass

import scala.language.higherKinds

import es.urjc.etsii.adt._

trait Functor[F[_]] {
  def map[A, B](value: F[A])(f: A => B): F[B]
}

object Functor {

  implicit val opcFunctor = new Functor[Opcion] {
    def map[A, B](value: Opcion[A])(f: A => B): Opcion[B] = value.map(f)
  }

  implicit val listFunctor = new Functor[Lista] {
    def map[A, B](value: Lista[A])(f: A => B): Lista[B] = value.map(f)
  }
}

object FunctorOps {

  def distribute[F[_]: Functor, A, B](fab: F[(A, B)]): (F[A], F[B]) = {
    val functor = implicitly[Functor[F]]
    (functor.map(fab)(_._1), functor.map(fab)(_._2))
  }

  def replace[F[_]: Functor, A, B](fa: F[A], b: B): F[B] = {
    val functor = implicitly[Functor[F]]
    functor.map(fa)(_ => b)
  }

  def strengthR[F[_]: Functor, A, B](fa: F[A], b: B): F[(A, B)] = {
    val functor = implicitly[Functor[F]]
    functor.map(fa)(a => (a, b))
  }
}

object FunctorMain extends App {
  import FunctorOps._

  println("\nDISTRIBUTE:")
  println(distribute(Lista(1 -> "one", 2 -> "two", 3 -> "three", 4 -> "four")))
  println(distribute(Opcion(1 -> "one")))

  println("\nREPLACE:")
  println(replace(Lista(1.0, 2.0, 3.0), "x"))
  println(replace(Opcion('a'), "x"))

  println("\nSTRENGTH_R:")
  println(strengthR(Lista(1, 2, 3), "R"))
  println(strengthR(Opcion("hello"), "R"))
}
