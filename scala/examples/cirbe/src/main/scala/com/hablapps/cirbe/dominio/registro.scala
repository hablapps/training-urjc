package com.hablapps.cirbe.dominio

sealed trait Registro {
  def nombre: String
  def errores: List[Error]
}

case class DB010(
    relacion: Relacion,
    nombre: String = "",
    errores: List[Error] = List()) extends Registro

case class DB020(
    operacion: Operacion,
    nombre: String = "",
    errores: List[Error] = List()) extends Registro

case class Finalizar(errores: List[Error] = List()) extends Registro {
  val nombre = "Finalizar"
}
