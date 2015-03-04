package es.urjc.etsii

class Gestion(val relacion: Map[Curso, List[Alumno]] = Map()) {

  def inscribirAlumno(alumno: Alumno, curso: Curso): Option[Gestion] = {
    val alumnos = relacion.get(curso).getOrElse(List())
    if ((alumnos.size >= curso.limiteAlumnos) || alumnos.contains(alumno))
      None
    else
      Some(new Gestion(relacion + (curso -> (alumno :: alumnos))))
  }
}
