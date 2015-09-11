package com.hablapps.cirbe.proceso.crgopes

import scalaz._, Scalaz._

import Crgopes._
import com.hablapps.cirbe.dominio._

object StateInterpreter {

  type StateCirbe[A] = State[Cirbe, A]
  val monad = implicitly[Monad[StateCirbe]]
  import monad._

  def toState = new (InstruccionCrgopes ~> StateCirbe) {
    def apply[A](ins: InstruccionCrgopes[A]): StateCirbe[A] = ins match {
      // case GetCrgopes(id) => for {
      //   _ <- gets(cirbe => cirbe.procesos.find())
      // } yield valor
      case Remitir(_) => { println("Remitiendo a BdE..."); point(()) }
      case SolicitarConfirmacion(_) => point(true)
      case Validar(r, v) => point(v.run(r))
      // case Alta(a) => for {
      //   c <- gets((s: CrgopesState) => s._1)
      //   _ <- modify((s: CrgopesState) => (s._1 + 1, s._2 + (s._1 -> a)))
      // } yield c
      // case Evaluar(ra) => for {
      //   m <- gets((s: CrgopesState) => s._2)
      // } yield m(ra).asInstanceOf[A]
      // case Actualizar(ra, a) => for {
      //   _ <- modify((s: CrgopesState) => (s._1, s._2.updated(ra, a)))
      // } yield ()
      // case Validar(rr, v) => for {
      //   m <- gets((s: CrgopesState) => s._2)
      // } yield v.run(m(rr).asInstanceOf[Registro])
      // case Enviar(rs) => (implicitly[Monad[StateCrgopes]].traverse(rs) { r =>
      //   for {
      //     v <- gets((s: CrgopesState) => s._2(r))
      //     _ = println(s"=> Enviando registro '$v' a BdE")
      //   } yield ()
      // }) as (())
    }
  }
}
