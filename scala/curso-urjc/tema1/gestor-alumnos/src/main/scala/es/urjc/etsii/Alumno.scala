
/* El contenido de este fichero (en este caso, la clase `Alumno`)
 * cae dentro del paquete `es.urjc.etsii`.
 */
package es.urjc.etsii

/* Declaración de una clase `Alumno`. Adicionalmente, en estas líneas
 * también se está declarando un constructor, dos atributos y un
 * "getter" para cada uno de ellos (que tiene el mismo nombre que el
 * propio atributo). El equivalente en Java es mucho más ruidoso por
 * la gran redundancia necesaria para llevar a cabo el mismo trabajo.
 */
class Alumno(
    val nombre: String, 
    val apellidos: String) {

  /* Esto es un método de la clase, de nombre `nick`. Su misión es la de
   * generar un nick para el alumno, utilizando "string interpolation"
   * en la tarea. Por ejemplo, para la instancia `Alumno("Juan", "Pérez")`
   * el método devolverá el string `"juan_perez"`
   */
  def nick: String = s"${nombre.toLowerCase}_${apellidos.toLowerCase}"

  override def toString = nick
}
