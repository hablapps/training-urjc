package com.hablapps.cirbe.dominio

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

trait Registro

case class DB020(
  tipoProducto: TProducto,
  tipoRiesgo: TRiesgo) extends Registro
