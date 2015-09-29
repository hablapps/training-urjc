package com.hablapps.cirbe.dominio

case class Crgopes(
  nombre: String,
  estado: EstadoCrgopes = Activo,
  db010s: List[Id[DB010]] = List(),
  db020s: List[Id[DB020]] = List())
