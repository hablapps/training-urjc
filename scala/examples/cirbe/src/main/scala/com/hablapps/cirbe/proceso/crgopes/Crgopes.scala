package com.hablapps.cirbe.proceso.crgopes

import scala.reflect.runtime.universe._

import scalaz._, Scalaz._

import com.hablapps.cirbe.dominio._
import com.hablapps.cirbe.dominio.Id
import com.hablapps.cirbe.proceso.validacion._

object Crgopes {

  // Primitivas

  sealed trait InstruccionCrgopes[A]

  case class Declarar[R <: Registro](registro: R, crgopes: Id[Crgopes])
    extends InstruccionCrgopes[String]

  case class GetCrgopes(crgopes: Id[Crgopes])
    extends InstruccionCrgopes[Option[Crgopes]]

  case class Remitir(registros: List[Id[Registro]])
    extends InstruccionCrgopes[Unit]

  case class SolicitarConfirmacion(relacion: List[(String, Resultado)])
    extends InstruccionCrgopes[Boolean]

  case class Validar[R <: Registro : TypeTag](registro: Id[R], validacion: Validacion[R])
      extends InstruccionCrgopes[Resultado] {
    val typeTag = implicitly[TypeTag[R]]
  }

  type ProgramaCrgopes[A] = Free[InstruccionCrgopes, A]

  // "Smart Constructors"

  def returns[A](a: A): ProgramaCrgopes[A] = Free.point(a)

  def declarar[R <: Registro](registro: R, crgopes: Id[R]) =
    Free.liftF[InstruccionCrgopes, String](Declarar(registro, crgopes))

  def validar[R <: Registro : Validacion : TypeTag](registro: Id[R]) =
    Free.liftF[InstruccionCrgopes, Resultado](
      Validar(registro, implicitly[Validacion[R]]))

  def remitir(registros: List[Id[Registro]]) =
    Free.liftF[InstruccionCrgopes, Unit](Remitir(registros))

  def solicitarConfirmacion(relacion: List[(String, Resultado)]) =
    Free.liftF[InstruccionCrgopes, Boolean](SolicitarConfirmacion(relacion))

  def getCrgopes(proceso: Id[Proceso]) =
    Free.liftF[InstruccionCrgopes, Option[Crgopes]](GetCrgopes(proceso))

  // Combinadores

  val monad = Monad[ProgramaCrgopes]
  import monad.{ traverse, whenM }

  def paraTodos[A, B](xs: List[A])(f: A => ProgramaCrgopes[B]): ProgramaCrgopes[List[B]] =
    traverse(xs)(f)

  def remitirEnvio(crgopes_id: Id[Crgopes])(
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
