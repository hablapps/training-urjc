package com.hablapps.cirbe.dominio

sealed trait Registro {
  def id: String
  def errores: List[Error]
}

case class DB010(
  relacion: Relacion,
  errores: List[Error] = List())
    extends Registro {
  def id: Id[DB010] =
    s"DB010_${relacion.codigoOperacion}_${relacion.codigoTitular}"
}

case class DB020(
  operacion: Operacion,
  errores: List[Error] = List())
    extends Registro {
  def id: Id[DB020] = s"DB020_${operacion.codigo}"
}

case class Finalizar(errores: List[Error] = List()) extends Registro {
  def id: Id[Finalizar] = "Finalizar"
}
