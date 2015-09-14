package com.hablapps.cirbe.dominio

case class Crgopes(
  id: String,
  db010s: List[DB010] = List(),
  db020s: List[DB020] = List())
