package com.hablapps.cirbe.test

import scalaz._, Scalaz._

import com.hablapps.cirbe.dominio._
import com.hablapps.cirbe.dominio.Id
import com.hablapps.cirbe.proceso.crgopes._
import com.hablapps.cirbe.proceso.crgopes.Crgopes._

sealed trait Precond

case class RequiereProceso(
  nombre: Option[String] = None,
  preconds: List[Proceso => Boolean] = List()) extends Precond

case class RequiereCrgopes(
  nombre: Option[String] = None,
  context: Option[Id[Proceso]] = None,
  preconds: List[Crgopes => Boolean] = List()) extends Precond

case class RequiereDB010(
  nombre: Option[String] = None,
  context: Option[Id[Crgopes]] = None,
  // TODO: Integrar con validaciones!
  preconds: List[DB010 => Boolean] = List()) extends Precond

case class RequiereDB020(
  nombre: Option[String] = None,
  context: Option[Id[Crgopes]] = None,
  preconds: List[DB020 => Boolean] = List()) extends Precond
