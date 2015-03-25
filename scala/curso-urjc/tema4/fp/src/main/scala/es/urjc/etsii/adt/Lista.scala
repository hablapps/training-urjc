package es.urjc.etsii.adt

sealed trait Lista[+A] {

  def map[B](f: A => B): Lista[B] = this match {
    case Nada => Nada
    case Cons(h, tail) => Cons(f(h), tail.map(f))
  }

  def ++[B >: A](other: Lista[B]): Lista[B] = this match {
    case Nada => other
    case Cons(x, xs) => Cons(x, xs ++ other)
  }

  def foldLeft[B](z: B)(f: (B, A) => B): B = this match {
    case Nada => z
    case Cons(x, xs) => xs.foldLeft(f(z, x))(f)
  }

  def flatMap[B](f: A => Lista[B]): Lista[B] = this match {
    case Nada => Nada
    case Cons(a, as) => f(a) ++ as.flatMap(f)
  }
}

case class Cons[A](cabeza: A, cola: Lista[A]) extends Lista[A]

case object Nada extends Lista[Nothing]

object Lista {
  def apply[A](args: A*): Lista[A] = args.toList match {
    case Nil => Nada
    case a :: as => Cons(a, apply(as: _*))
  }
}
