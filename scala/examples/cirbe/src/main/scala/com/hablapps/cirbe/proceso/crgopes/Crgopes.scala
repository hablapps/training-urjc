package com.hablapps.cirbe.proceso.crgopes

import scala.reflect.runtime.universe._

import scalaz._, Scalaz._

import com.hablapps.cirbe.dominio._
import com.hablapps.cirbe.dominio.Id
import com.hablapps.cirbe.proceso.validacion._

object Crgopes {

  // Primitivas

  sealed trait InstruccionCrgopes[A]

  case class Aseverar(condicion: Boolean, descripcion: Option[String] = None)
    extends InstruccionCrgopes[Unit]

  case class Fallar[A](mensaje: String)
    extends InstruccionCrgopes[A]

  case class GetCrgopes(crgopes: Id[Crgopes])
    extends InstruccionCrgopes[Option[Crgopes]]

  case class GetRegistro[R <: Registro : TypeTag](registro: Id[R])
      extends InstruccionCrgopes[R] {
    val typeTag = implicitly[TypeTag[R]]
  }

  case class PutRegistro[R <: Registro](registro: R, crgopes: Id[Crgopes])
    extends InstruccionCrgopes[Id[R]]

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

  def aseverar(condicion: Boolean, descripcion: Option[String] = None)
      : ProgramaCrgopes[Unit] =
    Free.liftF[InstruccionCrgopes, Unit](Aseverar(condicion, descripcion))

  def aseverar(condicion: Boolean, descripcion: String): ProgramaCrgopes[Unit] =
    aseverar(condicion, Option(descripcion))

  def putRegistro[R <: Registro](registro: R, crgopes: Id[Crgopes]) =
    Free.liftF[InstruccionCrgopes, Id[R]](PutRegistro(registro, crgopes))

  def validar[R <: Registro : Validacion : TypeTag](registro: Id[R]) =
    Free.liftF[InstruccionCrgopes, Resultado](
      Validar(registro, implicitly[Validacion[R]]))

  def remitir(registros: List[Id[Registro]]) =
    Free.liftF[InstruccionCrgopes, Unit](Remitir(registros))

  def solicitarConfirmacion(relacion: List[(String, Resultado)]) =
    Free.liftF[InstruccionCrgopes, Boolean](SolicitarConfirmacion(relacion))

  def getCrgopes(crgopes_id: Id[Crgopes]) =
    Free.liftF[InstruccionCrgopes, Option[Crgopes]](GetCrgopes(crgopes_id))

  def getRegistro[R <: Registro : TypeTag](registro: Id[R]) =
    Free.liftF[InstruccionCrgopes, R](GetRegistro(registro))

  def fallar[A](mensaje: String) =
    Free.liftF[InstruccionCrgopes, A](Fallar[A](mensaje))

  // Combinadores

  val monad = Monad[ProgramaCrgopes]
  import monad.{ traverse, whenM }

  def paraTodos[A, B](xs: List[A])(f: A => ProgramaCrgopes[B]): ProgramaCrgopes[List[B]] =
    traverse(xs)(f)

  implicit class FilterHelper[A](programa: ProgramaCrgopes[A]) {
    def withFilter(f: A => Boolean): ProgramaCrgopes[A] = for {
      a <- programa
      _ <- if (f(a))
        monad.point(())
      else
        fallar[A]("withFilter - no se cumple la condición")
    } yield a
  }

  def declarar[R <: Registro](registro: R, crgopes_id: Id[Crgopes]) = for {
    optCrgopes <- getCrgopes(crgopes_id)
    if optCrgopes.fold(false)(crgopes => crgopes.estado == Activo)
    id <- putRegistro(registro, crgopes_id)
  } yield id

  def finalizar(crgopes: Id[Crgopes]) = declarar(Finalizar(), crgopes)

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
