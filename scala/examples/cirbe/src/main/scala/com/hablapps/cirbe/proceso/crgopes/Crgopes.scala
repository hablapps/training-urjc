package com.hablapps.cirbe.proceso.crgopes

import scalaz._, Scalaz._

import com.hablapps.cirbe.dominio._
import com.hablapps.cirbe.proceso.validacion._

object Crgopes {

  // Primitivas

  sealed trait InstruccionCrgopes[A]

  case class Declarar[R <: Registro](registro: R, crgopes_id: String)
    extends InstruccionCrgopes[String]

  case class GetCrgopes(crgopes_id: String)
    extends InstruccionCrgopes[Option[Crgopes]]

  case class Remitir(registros: List[Registro])
    extends InstruccionCrgopes[Unit]

  case class SolicitarConfirmacion(relacion: List[(Registro, Resultado)])
    extends InstruccionCrgopes[Boolean]

  case class Validar[R <: Registro](registro: R, validacion: Validacion[R])
    extends InstruccionCrgopes[Resultado]

  type ProgramaCrgopes[A] = Free[InstruccionCrgopes, A]

  // "Smart Constructors"

  def returns[A](a: A): ProgramaCrgopes[A] = Free.point(a)

  def declarar[R <: Registro](registro: R, crgopes_id: String) =
    Free.liftF[InstruccionCrgopes, String](Declarar(registro, crgopes_id))

  def validar[R <: Registro : Validacion](registro: R) =
    Free.liftF[InstruccionCrgopes, Resultado](
      Validar(registro, implicitly[Validacion[R]]))

  def remitir(registros: List[Registro]) =
    Free.liftF[InstruccionCrgopes, Unit](Remitir(registros))

  def solicitarConfirmacion(relacion: List[(Registro, Resultado)]) =
    Free.liftF[InstruccionCrgopes, Boolean](SolicitarConfirmacion(relacion))

  def getCrgopes(crgopes_id: String) =
    Free.liftF[InstruccionCrgopes, Option[Crgopes]](GetCrgopes(crgopes_id))

  // Combinadores

  val monad = Monad[ProgramaCrgopes]
  import monad.{ traverse, whenM }

  def paraTodos[A, B](xs: List[A])(f: A => ProgramaCrgopes[B]): ProgramaCrgopes[List[B]] =
    traverse(xs)(f)

  def remitirEnvio(crgopes_id: String)(
      implicit v: Validacion[DB010], w: Validacion[DB020]): ProgramaCrgopes[Unit] = {
    for {
      crgopes  <- getCrgopes(crgopes_id).map(_.get)
      rel010   <- paraTodos(crgopes.db010s)(validar(_)).map(crgopes.db010s.zip(_))
      rel020   <- paraTodos(crgopes.db020s)(validar(_)).map(crgopes.db020s.zip(_))
      relacion =  rel010 ++ rel020
      ok       <- solicitarConfirmacion(relacion)
      _        <- whenM(ok)(remitir(relacion.filter(_._2.esValido).map(_._1)))
    } yield ()
  }
}
