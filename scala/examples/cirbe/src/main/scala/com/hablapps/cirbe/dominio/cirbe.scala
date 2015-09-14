package com.hablapps.cirbe.dominio

case class Cirbe(
  procesos: List[Proceso] = List(),
  titulares: List[Titular] = List(),
  operaciones: List[Operacion] = List(),
  relaciones: List[Relacion] = List())

case class Titular(
  codigo: String,
  nombre: String,
  apellidos: String)

case class Operacion(
  codigo: String,
  tipoProducto: TProducto,
  tipoRiesgo: TRiesgo)

case class Relacion(
  codigoOperacion: String,
  codigoTitular: String)
