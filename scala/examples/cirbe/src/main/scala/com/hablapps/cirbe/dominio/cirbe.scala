package com.hablapps.cirbe.dominio

case class Cirbe(
  procesos: List[Proceso],
  titulares: List[Titular],
  operaciones: List[Operacion],
  relaciones: List[Relacion])

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
