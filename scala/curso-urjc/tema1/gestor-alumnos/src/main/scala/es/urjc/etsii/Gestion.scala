package es.urjc.etsii

class Gestion(val relacion: Map[Curso, List[Alumno]]) {

  def inscribirAlumno(alumno: Alumno, curso: Curso): Option[Gestion] = {
    val alumnos = relacion(curso)
    if ((alumnos.size >= curso.limiteAlumnos) || alumnos.contains(alumno))
      None
    else
      Some(new Gestion(relacion + (curso -> (alumno :: alumnos))))
  }
}
