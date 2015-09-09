package com.hablapps.cirbe.proceso.crgope

import scalaz._, Scalaz._

import com.hablapps.cirbe.dominio._
import com.hablapps.cirbe.proceso.validacion._

object CRGOPE {

  type Ref[A] = Integer

  sealed trait CRGOPE[A]
  case class Alta[A](a: A) extends CRGOPE[Ref[A]]
  case class Evaluar[A](ra: Ref[A]) extends CRGOPE[A]
  case class Actualizar[A](ra: Ref[A], a: A) extends CRGOPE[Unit]
  case class Validar[R <: Registro](
    rr: Ref[R], v: Validacion[R]) extends CRGOPE[Resultado]
  case class Enviar[R <: Registro](rs: List[Ref[R]]) extends CRGOPE[Unit]

  type ProgramaCRGOPE[A] = Free[CRGOPE, A]

  // "Smart Constructors"

  def returns[A](a: A): ProgramaCRGOPE[A] = Free.point(a)

  def alta[A](a: A): ProgramaCRGOPE[Ref[A]] =
    Free.liftF[CRGOPE, Ref[A]](Alta[A](a))

  def evaluar[A](ea: Ref[A]): ProgramaCRGOPE[A] =
    Free.liftF[CRGOPE, A](Evaluar[A](ea))

  def actualizar[A](ea: Ref[A])(a: A): ProgramaCRGOPE[Unit] =
    Free.liftF[CRGOPE, Unit](Actualizar(ea, a))

  def validar[R <: Registro { type This = R }](
      rr: Ref[R])(implicit v: Validacion[R]): ProgramaCRGOPE[Resultado] =
    Free.liftF[CRGOPE, Resultado](Validar(rr, v))

  def enviar[R <: Registro](rs: List[Ref[R]]): ProgramaCRGOPE[Unit] =
    Free.liftF[CRGOPE, Unit](Enviar(rs))

  // Combinadores

  def paraTodos[A, B](xs: List[A])(f: A => ProgramaCRGOPE[B]): ProgramaCRGOPE[List[B]] =
    Monad[ProgramaCRGOPE].traverse(xs)(f)

  def paraTodas[A, B](xs: List[A])(f: A => ProgramaCRGOPE[B]): ProgramaCRGOPE[List[B]] =
    paraTodos[A, B](xs)(f)

  def modificar[A](ea: Ref[A])(f: A => A): ProgramaCRGOPE[Unit] = for {
    a <- evaluar(ea)
    _ <- actualizar(ea)(f(a))
  } yield ()

  def altaBorrador[R <: Registro { type This = R }](
      r: R): ProgramaCRGOPE[Ref[R]] = for {
    er <- alta(r)
    _  <- actualizarEstado(er, Borrador)
  } yield er

  def actualizarEstado[R <: Registro { type This = R }](
      rr: Ref[R],
      estado: EstadoRegistro): ProgramaCRGOPE[Unit] =
    modificar(rr)(r => r.setEstado(estado))

  def actualizarErrores[R <: Registro { type This = R }](
      rr: Ref[R],
      errores: List[Error]): ProgramaCRGOPE[Unit] =
    modificar(rr)(r => r.setErrores(errores))

  def aplicarValidaciones[R <: Registro { type This = R } : Validacion](
      rr: Ref[R]): ProgramaCRGOPE[Resultado] = for {
    res <- validar(rr)
    _   <- res match {
      case Invalido(errs) => actualizarErrores(rr, errs.toList)
      case Valido => Free.point[CRGOPE, Unit](())
    }
  } yield res
}
