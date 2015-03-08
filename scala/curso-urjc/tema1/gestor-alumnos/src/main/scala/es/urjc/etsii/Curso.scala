package es.urjc.etsii

class Curso(
  val nombre: String,
  val limiteAlumnos: Int = 30,
  val descripcion: Option[String] = None)
