package es.urjc.etsii

object ListaMonad extends App {

  case class School(id: String, desc: Opcion[String])

  case class Student(name: String, last: String, school: Opcion[School] = Ninguno)

  val etsii = School(
    "ETSII",
    Algun("Escuela Técnica Superior de Ingeniería Informática"))

  implicit val map: Map[String, Student] = Map(
    "123456789A" -> Student("john", "smith"),
    "987654321B" -> Student("peter", "parker", Algun(etsii)))

}
