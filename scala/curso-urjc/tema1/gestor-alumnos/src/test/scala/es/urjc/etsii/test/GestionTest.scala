package es.urjc.etsii.test

import es.urjc.etsii._
import org.scalatest._

class GestionTest extends FlatSpec with Matchers {

  val maria = new Alumno("María", "Pérez")
  val jose  = new Alumno(nombre = "José", apellidos = "García")
  val ana   = new Alumno(apellidos = "López", nombre = "Ana")
  val paco  = new Alumno("Paco", "González")

  val curso = new Curso(
    nombre = "Programación Funcional en Scala", 
    descripcion = Some(""), 
    limiteAlumnos = 3)

  "El gestor" should "permitir a un alumno inscribirse si hay plazas" in {
    val gtn = new Gestion(Map(curso -> List(maria, jose)))
    val optGtn = gtn.inscribirAlumno(ana, curso)

    optGtn.get.relacion should be (Map(curso -> List(ana, maria, jose)))
  }

  it should "rechazar a un alumno si ya está inscrito" in {
    val gtn = new Gestion(Map(curso -> List(maria, jose)))
    
    gtn.inscribirAlumno(maria, curso) should be (None)
  }

  it should "rechazar a un alumno si no quedan plazas" in {
    val gtn = new Gestion(Map(curso -> List(maria, jose, ana)))
    
    gtn.inscribirAlumno(paco, curso) should be (None)
  }
}
