package com.hablapps.cirbe.test

import scalaz._, Scalaz._

import com.hablapps.cirbe.dominio._
import com.hablapps.cirbe.dominio.Id
import com.hablapps.cirbe.proceso.crgopes._
import com.hablapps.cirbe.proceso.crgopes.Crgopes._

sealed trait Precond {

  private def check[A](ctx: Option[A], p: List[A]) =
    p.nonEmpty && ctx.fold(true)(p.contains(_))

  def cumple(delta: Delta): Boolean = this match {
    case RequiereProceso(_, _)      => true
    case RequiereCrgopes(_, ctx, _) => check(ctx, delta.procesos)
    case RequiereDB010(_, ctx, _)   => check(ctx, delta.crgopes)
    case RequiereDB020(_, ctx, _)   => check(ctx, delta.crgopes)
  }
}

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

case class Delta(
    procesos: List[Id[Proceso]] = List.empty,
    crgopes: List[Id[Crgopes]] = List.empty,
    registros: List[Id[Registro]] = List.empty) {
  def addProceso(pro: Proceso) = copy(procesos = pro.nombre +: procesos)
  def addCrgopes(crg: Crgopes) = copy(crgopes = crg.nombre +: crgopes)
  def addRegistro(reg: Registro) = copy(registros = reg.nombre +: registros)
}
