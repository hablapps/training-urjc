package com.hablapps.cirbe.proceso.crgope

import scalaz._, Scalaz._

import com.hablapps.cirbe.dominio._

object CRGOPE {

  // Primitivas del Lenguaje

  sealed trait CRGOPE[A]
  case class DeclararRegistro(registro: Registro) extends CRGOPE[Unit]
  case object EnviarDeclaraciones extends CRGOPE[Unit]

  type ProgramaCRGOPE[A] = Free[CRGOPE, A]

  // "Smart Constructors"

  def returns[A](a: A): ProgramaCRGOPE[A] = Free.point(a)

  def declararRegistro(registro: Registro): ProgramaCRGOPE[Unit] = ???
    // Free.liftF(DeclararRegistro(registro))

  def enviarDeclaraciones: ProgramaCRGOPE[Unit] = ???
    // Free.liftF(EnviarDeclaraciones)
}
