package com.hablapps.cirbe.test

import scala.language.higherKinds

import scalaz._, Scalaz._

import org.scalacheck._

object Precond {

  sealed trait PrecondIns[A]

  case class Demand[F[_], A](ins: F[A]) extends PrecondIns[Free[F, A]]

  case class Ensure[F[_], A](query: Free[F, A], matcher: A => Unit)
      extends PrecondIns[Free[F, Unit]]

  type Precond[A] = Free[PrecondIns, A]

  def demand[F[_], A](i: F[A]): Precond[Free[F, A]] =
    Free.liftF[PrecondIns, Free[F, A]](Demand(i))

  def ensure[F[_], A](q: Free[F, A])(m: A => Unit): Precond[Free[F, Unit]] =
    Free.liftF[PrecondIns, Free[F, Unit]](Ensure(q, m))

  def toFreeFA[F[_]] = {
    type FreeF[A] = ({type FreeF[A] = Free[F, A]})#FreeF[A]
    new (PrecondIns ~> FreeF) {
      def apply[A](ins: PrecondIns[A]): FreeF[A] = ins match {
        case Demand(ins2: F[A]) => Free.liftF(ins2)
        //case Ensure(query: Free[F, A], matcher) => ???
        case _ => ???
      }
    }
  }

  // def toId = new (PrecondIns ~> Id) {
  //   def apply[A](ins: PrecondIns[A]): Id[A] = ins match {
  //     case Demand(ins2) => Free.liftF(ins2)
  //     case Ensure(query, matcher) => ??? // XXX: necesito un `test` genérico!
  //   }
  // }
}
