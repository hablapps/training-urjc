package es.urjc.etsii.test

import es.urjc.etsii._
import Gestion._
import org.scalatest._

class GestionTest extends FlatSpec with Matchers {

  val maria = new Alumno("María", "Pérez", true)
  val jose  = new Alumno("José", "García", false)
  val ana   = new Alumno("Ana", "López", false)

  val curso = new Curso("Programación Funcional en Scala", 90, None)

  "El gestor" should "generar el precio correcto para cada alumno" in {
    val importeMaria = calcularImporteAlumno(curso, maria) 
    val importeJose = calcularImporteAlumno(curso, jose)

    importeMaria should be (curso.precio + (curso.precio * 0.2))
    importeJose  should be (curso.precio)
  }

  it should "generar el precio correcto para un grupo de alumnos" in {
    val total = 
      calcularImporteAlumno(curso, maria) +
        calcularImporteAlumno(curso, jose) +
        calcularImporteAlumno(curso, ana)

    calcularImporteAlumnos(curso, List(maria, jose, ana)) should be (total)
  }
}
