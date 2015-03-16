
sealed trait Opcion[+A]

case class Algun[A](valor: A) extends Opcion[A] // Some

case object Ninguno extends Opcion[Nothing]     // None

object Opcion {
  def map[A, B](op: Opcion[A])(f: A => B): Opcion[B] = op match {
    case Algun(a) => Algun(f(a))
    case _ => Ninguno
  }
}
