package com.hablapps.cirbe.test

import scalaz._, Scalaz._

import org.scalacheck._
import Gen._
import Arbitrary.arbitrary

import com.hablapps.cirbe.dominio._
import com.hablapps.cirbe.dominio.Id
import com.hablapps.cirbe.proceso.crgopes.Crgopes._

object GenInterpreter {

  case class Delta(
    procesos: List[Id[Proceso]] = List.empty,
    crgopes: List[Id[Crgopes]] = List.empty,
    db010s: List[Id[DB010]] = List.empty,
    db020s: List[Id[DB020]] = List.empty)

  implicit val genMonad = new Functor[Gen] with Monad[Gen] {
    def point[A](a: => A): Gen[A] = a
    def bind[A, B](fa: Gen[A])(f: A => Gen[B]): Gen[B] = fa flatMap f
  }

  implicit class listHelper[A](p: List[A]) {
    def condCat(cond: Boolean, q: List[A]): List[A] = if (cond) p ++ q else p
    def condCat(cond: Boolean, e: A): List[A] = condCat(cond, List(e))
  }

  implicit class ToGenHelper[A](programa: List[Precond]) {

    val m  = Monad[Gen]
    val m2 = Monad[ProgramaCrgopes]

    def toGen: Gen[ProgramaCrgopes[Unit]] = {

      type P[A]  = ProgramaCrgopes[A]
      type GP[A] = Gen[P[A]]

      def unir[A, B]: (GP[A], GP[B]) => GP[Unit] =
        m.lift2[P[A], P[B], P[Unit]]((pa, pb) => m2.apply2(pa, pb)((a, b) => ()))

      def toG(prog: List[Precond], delta: Delta): GP[Unit] = {

        val gens: List[GP[Unit]] =
          List(unir(crearProceso(Proceso("201511")), toG(prog, delta)))
            .condCat(prog.isEmpty, returns(()))

        oneOf(gens.head, gens.tail.head, gens.tail.tail:_*)
      }

      toG(programa, Delta())
    }
  }
}
