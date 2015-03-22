package es.urjc.etsii.typeclass

import scala.language.higherKinds

import es.urjc.etsii.adt._

trait Monad[M[_]] extends Functor[M] {

  def unit[A](value: A): M[A]

  def flatMap[A, B](m: M[A])(f: A => M[B]): M[B]

  def map[A, B](m: M[A])(f: A => B): M[B] = flatMap(m)(a => unit(f(a)))

  def map2[A, B, C](ma: M[A], mb: M[B])(f: (A, B) => C): M[C] =
    flatMap(ma)(a => map(mb)(b => f(a, b)))

  def sequence[A](lma: List[M[A]]): M[List[A]] =
    lma.foldRight(unit(List[A]()))((ma, mla) => map2(ma, mla)(_ :: _))

  def product[A, B](ma: M[A], mb: M[B]): M[(A, B)] = map2(ma, mb)((_, _))
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

  implicit def cualquieraMonad[I] = new Monad[({type f[x] = Cualquiera[I, x]})#f] {

    def unit[D](d: D) = Derecha[I, D](d)

    def flatMap[D, E](cualq: Cualquiera[I, D])(f: D => Cualquiera[I, E]) =
      cualq.flatMap(f)
  }
}
