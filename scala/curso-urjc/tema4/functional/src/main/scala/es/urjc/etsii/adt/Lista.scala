package es.urjc.etsii.adt

sealed trait Lista[+A] {

  def map[B](f: A => B): Lista[B] = this match {
    case Nada => Nada
    case Cons(x, xs) => Cons(f(x), xs.map(f))
  }
}

case class Cons[A](cabeza: A, cola: Lista[A]) extends Lista[A]

case object Nada extends Lista[Nothing]

object Lista {

  def apply[A](args: A*): Lista[A] = args.toList match {
    case Nil => Nada
    case a :: as => Cons(a, apply(as:_*))
  }
}
