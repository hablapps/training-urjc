package es.urjc.etsii

class Alumno(
    val nombre: String, 
    val apellidos: String) {

  def nick: String = s"${nombre.toLowerCase}_${apellidos.toLowerCase}"

  override def toString = nick
}
