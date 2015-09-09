package com.hablapps.cirbe.dominio

trait EstadoDeclaracion
case object AceptacionTotal extends EstadoDeclaracion
case object AceptacionParcial extends EstadoDeclaracion
case object RechazoTotal extends EstadoDeclaracion

trait Error
case object R2008 extends Error
case object R2009 extends Error

trait TProducto
case object V39 extends TProducto
case object V40 extends TProducto
case object V48 extends TProducto
case object V54 extends TProducto

trait TRiesgo
case object ZZZ extends TRiesgo

trait EstadoRegistro
case object Borrador  extends EstadoRegistro
case object Remitido  extends EstadoRegistro
case object Aceptado  extends EstadoRegistro
case object Rechazado extends EstadoRegistro

trait Registro { self =>

  type This <: Registro

  val estado: EstadoRegistro
  val errores: List[Error]

  def setEstado(estado: EstadoRegistro): self.This
  def setErrores(errores: List[Error]): self.This
}

case class DB020(
    tipoProducto: TProducto,
    tipoRiesgo: TRiesgo,
    estado: EstadoRegistro = Borrador,
    errores: List[Error] = List.empty) extends Registro {

  type This = DB020

  def setEstado(estado: EstadoRegistro): This = copy(estado=estado)
  def setErrores(errores: List[Error]): This = copy(errores=errores)
}
