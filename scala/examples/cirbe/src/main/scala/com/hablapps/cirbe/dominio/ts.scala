package com.hablapps.cirbe.dominio

trait TError // TODO: TError => Error
case object R2008 extends TError
case object R2009 extends TError

trait TProducto
case object V39 extends TProducto
case object V40 extends TProducto
case object V48 extends TProducto
case object V54 extends TProducto

trait TRiesgo
case object ZZZ extends TRiesgo
