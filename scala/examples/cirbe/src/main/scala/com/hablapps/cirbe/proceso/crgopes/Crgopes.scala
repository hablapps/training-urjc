package com.hablapps.cirbe.proceso.Crgopess

import scalaz._, Scalaz._

import com.hablapps.cirbe.dominio._
import com.hablapps.cirbe.proceso.validacion._

object Crgopes {

  type Ref[A] = String

  sealed trait InstruccionCrgopes[A]

  case class Declarar[R <: Registro](registro: R, crgopes: Ref[Crgopes])
    extends InstruccionCrgopes[Ref[R]]

  case class Validar[R <: Registro](registro: R, validacion: Validacion[R])
    extends InstruccionCrgopes[Resultado]

  case class Remitir(crgopes: Ref[Crgopes])
    extends InstruccionCrgopes[List[(Registro, Resultado)]]

  case class SolicitarConfirmacion(relacion: List[(Registro, Resultado)])
    extends InstruccionCrgopes[Boolean]

  type ProgramaCrgopes[A] = Free[InstruccionCrgopes, A]

  // "Smart Constructors"

  def returns[A](a: A): ProgramaCrgopes[A] = Free.point(a)

  def declarar[R <: Registro](registro: R, crgopes: Ref[Crgopes]) =
    Free.liftF[InstruccionCrgopes, Ref[R]](Declarar(registro, crgopes))

  def validar[R <: Registro : Validacion](registro: R) =
    Free.liftF[InstruccionCrgopes, Resultado](
      Validar(registro, implicitly[Validacion[R]]))

  def remitir(crgopes: Ref[Crgopes]) =
    Free.liftF[InstruccionCrgopes, List[(Registro, Resultado)]](
      Remitir(crgopes))

  def solicitarConfirmacion(relacion: List[(Registro, Resultado)]) =
    Free.liftF[InstruccionCrgopes, Boolean](SolicitarConfirmacion(relacion))

  // // Combinadores
  //
  // def paraTodos[A, B](xs: List[A])(f: A => ProgramaCrgopes[B]): ProgramaCrgopes[List[B]] =
  //   Monad[ProgramaCrgopes].traverse(xs)(f)
  //
  // def paraTodas[A, B](xs: List[A])(f: A => ProgramaCrgopes[B]): ProgramaCrgopes[List[B]] =
  //   paraTodos[A, B](xs)(f)
  //
  // def modificar[A](ea: Ref[A])(f: A => A): ProgramaCrgopes[Unit] = for {
  //   a <- evaluar(ea)
  //   _ <- actualizar(ea)(f(a))
  // } yield ()
  //
  // def altaBorrador[R <: Registro { type This = R }](
  //     r: R): ProgramaCrgopes[Ref[R]] = for {
  //   er <- alta(r)
  //   _  <- actualizarEstado(er, Borrador)
  // } yield er
  //
  // def actualizarEstado[R <: Registro { type This = R }](
  //     rr: Ref[R],
  //     estado: EstadoRegistro): ProgramaCrgopes[Unit] =
  //   modificar(rr)(r => r.setEstado(estado))
  //
  // def actualizarErrores[R <: Registro { type This = R }](
  //     rr: Ref[R],
  //     errores: List[Error]): ProgramaCrgopes[Unit] =
  //   modificar(rr)(r => r.setErrores(errores))
  //
  // def aplicarValidaciones[R <: Registro { type This = R } : Validacion](
  //     rr: Ref[R]): ProgramaCrgopes[Resultado] = for {
  //   res <- validar(rr)
  //   _   <- res match {
  //     case Invalido(errs) => actualizarErrores(rr, errs.toList)
  //     case Valido => Free.point[Crgopes, Unit](())
  //   }
  // } yield res
}
