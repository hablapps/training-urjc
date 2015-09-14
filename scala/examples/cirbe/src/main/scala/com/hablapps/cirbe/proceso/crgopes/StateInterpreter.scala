package com.hablapps.cirbe.proceso.crgopes

import scalaz._, Scalaz._

import Crgopes._
import com.hablapps.cirbe.dominio._

object StateInterpreter {

  type StateCirbe[A] = State[Cirbe, A]
  val monad = implicitly[Monad[StateCirbe]]

  def toState = new (InstruccionCrgopes ~> StateCirbe) {
    def apply[A](ins: InstruccionCrgopes[A]): StateCirbe[A] = ins match {
      case Declarar(registro, crgopes_id) => for {
        cirbe    <- (get: StateCirbe[Cirbe])
        proceso  = cirbe.procesos.find(_.crgopes.exists(_.id == crgopes_id)).get
        crgopes  = cirbe.procesos.flatMap(_.crgopes).find(_.id == crgopes_id).get
        crgopes2 = registro match {
          case (r: DB010) => crgopes.copy(db010s = r +: crgopes.db010s)
          case (r: DB020) => crgopes.copy(db020s = r +: crgopes.db020s)
        }
        proceso2 = proceso.copy(crgopes = crgopes2 +: proceso.crgopes.diff(List(crgopes)))
        cirbe2 = cirbe.copy(procesos = proceso2 +: cirbe.procesos.diff(List(proceso)))
        _ <- put(cirbe2)
      } yield registro.id
      case GetCrgopes(crgopes_id) => for {
        cirbe <- get
      } yield cirbe.procesos.flatMap(_.crgopes).find(_.id == crgopes_id)
      case Remitir(rs) => {
        rs.foreach(r => println(s"cirbe> Remitiendo registro '${r.id}' a BdE"))
        monad.point(())
      }
      case SolicitarConfirmacion(_) => monad.point(true)
      case Validar(r, v) => monad.point(v.run(r))
    }
  }

  implicit class remove[A](xs: List[A]) {
    def remove(a: A) = xs.diff(List(a))
  }
}
