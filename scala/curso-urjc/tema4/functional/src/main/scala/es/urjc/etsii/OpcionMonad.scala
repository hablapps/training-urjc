package es.urjc.etsii

import es.urjc.etsii.adt._
import Opcion.MapExtend

object OpcionMonad extends App {

  case class School(id: String, desc: Opcion[String])

  case class Student(name: String, last: String, school: Opcion[School] = Ninguno)

  val etsii = School(
    "ETSII",
    Algun("Escuela Técnica Superior de Ingeniería Informática"))

  implicit val map: Map[String, Student] = Map(
    "123456789A" -> Student("john", "smith"),
    "987654321B" -> Student("peter", "parker", Algun(etsii)))


  // 1st version

  def upperCaseDesc(key: String, map: Map[String, Student]): Opcion[String] = {
    val st = map.getOpcion(key)
    if (st.isDefined) {
      val sch = st.get.school
      if (sch.isDefined) {
        val desc = sch.get.desc
        if (desc.isDefined) {
          desc.map(_.toUpperCase)
        } else {
          Ninguno
        }
      } else {
        Ninguno
      }
    } else {
      Ninguno
    }
  }

  println("FIRST VERSION: ")
  println(upperCaseDesc("UNKNOWNXXX", map)) // Ninguno
  println(upperCaseDesc("123456789A", map)) // Ninguno
  println(upperCaseDesc("987654321B", map)) // Algun("ESCUELA TÉCNICA...")


  // 2nd version

  def bind[A, B](value: Opcion[A])(f: A => Opcion[B]): Opcion[B] = value match {
    case Ninguno => Ninguno
    case Algun(a) => f(a)
  }

  def upperCaseDesc2(key: String, map: Map[String, Student]): Opcion[String] = {
    val desc = bind(bind(map.getOpcion(key))(st => st.school))(sch => sch.desc)
    desc.map(_.toUpperCase)
  }

  println("\nSECOND VERSION: ")
  println(upperCaseDesc2("UNKNOWNXXX", map))
  println(upperCaseDesc2("123456789A", map))
  println(upperCaseDesc2("987654321B", map))


  // 3rd version

  def upperCaseDesc3(key: String, map: Map[String, Student]): Opcion[String] = {
    (map.getOpcion(key)
      .bind(st  => st.school)
      .bind(sch => sch.desc)
      .map(desc => desc.toUpperCase))
  }

  println("\nTHIRD VERSION: ")
  println(upperCaseDesc3("UNKNOWNXXX", map))
  println(upperCaseDesc3("123456789A", map))
  println(upperCaseDesc3("987654321B", map))


  // 4rd version

  def upperCaseDesc4_1(key: String, map: Map[String, Student]): Opcion[String] = {
    (map.getOpcion(key)
      .flatMap(st  => st.school)
      .flatMap(sch => sch.desc)
      .map(desc => desc.toUpperCase))
  }

  // using for-comprehension
  def upperCaseDesc4_2(key: String, map: Map[String, Student]): Opcion[String] = 
    for {
      st   <- map.getOpcion(key)
      sch  <- st.school
      desc <- sch.desc
    } yield (desc.toUpperCase)

  println("\nFOURTH VERSION: ")
  println(upperCaseDesc4_2("UNKNOWNXXX", map))
  println(upperCaseDesc4_2("123456789A", map))
  println(upperCaseDesc4_2("987654321B", map))
}
