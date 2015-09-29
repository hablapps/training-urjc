package com.hablapps.cirbe.dominio

case class Proceso(nombre: String, crgopes: Option[Id[Crgopes]] = None)
