package es.urjc.etsii

class Curso(
  val nombre: String,
  val limiteAlumnos: Int,
  /* El siguiente atributo tiene el tipo `Option[String]` que nos
   * permite que dicho atributo sea "opcional", es decir, que puede
   * venir definido o no. Atendiendo a la lógica, esto nos permite
   * poder crear un curso que no tenga una descripción asociada. El
   * tipo `Option` tendrá una gran relevancia a lo largo del curso.
   * 
   * Es importante también destacar otro aspecto, el de los valores
   * por defecto. Se puede apreciar cómo después de la descripción del
   * atributo aparece el código `= None`. Esto nos indica que por
   * defecto, esta descripción no estará definida. Será útil cuando se
   * instancie un `Curso` (ver `GestionTest.scala`)
   */
  val descripcion: Option[String] = None)
