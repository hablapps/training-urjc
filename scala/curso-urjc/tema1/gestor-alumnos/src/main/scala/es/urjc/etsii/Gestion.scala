package es.urjc.etsii

object Gestion {

  def calcularImporteAlumno(curso: Curso, alumno: Alumno): Double = {
    if (alumno.esExterno)
      curso.precio + (curso.precio * 0.2)
    else
      curso.precio
  }

  def calcularImporteAlumnos(curso: Curso, alumnos: List[Alumno]): Double = {
    alumnos
      .map(calcularImporteAlumno(curso, _))
      .fold(0.0)(_ + _)
  }
}
