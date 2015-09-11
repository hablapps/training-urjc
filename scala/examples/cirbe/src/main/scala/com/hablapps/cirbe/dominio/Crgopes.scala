package com.hablapps.cirbe.dominio

case class Crgopes(
    proceso: Proceso,
    db010s: List[DB010] = List(),
    db020s: List[DB020] = List()) {
  def id: String = proceso.id
}
