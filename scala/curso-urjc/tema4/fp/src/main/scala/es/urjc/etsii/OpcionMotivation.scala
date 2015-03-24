package es.urjc.etsii

import es.urjc.etsii.adt._

object OpcionMotivation extends App {

  case class School(id: String, desc: Opcion[String] = Ninguno)

  case class Student(name: String, last: String, school: Opcion[School] = Ninguno)

  val etsii = School(
    "ETSII",
    Algun("Escuela Técnica Superior de Ingeniería Informática"))

  val map: Map[String, Student] = Map(
    "123456789A" -> Student("john", "smith"),
    "987654321B" -> Student("peter", "parker", Algun(etsii)))


  // 1st version

  implicit class MapExtender[A, B](map: Map[A, B]) {
    def getOpcion(key: A) = map.get(key) match {
      case None => Ninguno
      case Some(a) => Algun(a)
    }
  }

  def upperCaseDesc(dni: String, map: Map[String, Student]): Opcion[String] = {
    val st = map.getOpcion(dni)
    if (st.isDefined) {
      val sch = st.get.school
      if (sch.isDefined) {
        val desc = sch.get.desc
        if (desc.isDefined) {
          desc.map(d => d.toUpperCase)
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


  // 2nd version

  def applyIfOk[A, B](value: Opcion[A])(f: A => Opcion[B]): Opcion[B] = value match {
    case Ninguno => Ninguno
    case Algun(a) => f(a)
  }

  def upperCaseDesc2(dni: String, map: Map[String, Student]): Opcion[String] = {
    val desc = applyIfOk(applyIfOk(map.getOpcion(dni))(st => st.school))(sch => sch.desc)
    desc.map(_.toUpperCase)
  }


  // 3rd version

  def upperCaseDesc3_1(dni: String, map: Map[String, Student]): Opcion[String] = {
    map.getOpcion(dni) flatMap { st =>
      st.school flatMap { sch =>
        sch.desc.map(desc => desc.toUpperCase)
      }
    }
  }

  def upperCaseDesc3_2(dni: String, map: Map[String, Student]): Opcion[String] =
    for {
      st   <- map.getOpcion(dni)
      sch  <- st.school
      desc <- sch.desc
    } yield  (desc.toUpperCase)
}
