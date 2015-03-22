package es.urjc.etsii.adt

sealed trait Lista[+A] {

  def map[B](f: A => B): Lista[B] = this match {
    case Nada => Nada
    case Cons(x, xs) => Cons(f(x), xs.map(f))
  }

  def concat[B >: A](other: Lista[B]): Lista[B] = this match {
    case Nada => other
    case Cons(x, xs) => Cons(x, xs.concat(other))
  }

  def ++[B >: A](other: Lista[B]): Lista[B] = concat(other)

  def flatMap[B](f: A => Lista[B]): Lista[B] = this match {
    case Nada => Nada
    case Cons(x, xs) => f(x).concat(xs.flatMap(f))
  }

  def foldLeft[B](z: B)(f: (B, A) => B): B = this match {
    case Nada => z
    case Cons(x, xs) => xs.foldLeft(f(z, x))(f)
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
