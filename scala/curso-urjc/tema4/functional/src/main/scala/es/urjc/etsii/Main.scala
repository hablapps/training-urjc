package es.urjc.etsii

import typeclass._
import adt._

object Main extends App {

  def distributeLista[A, B](lst: Lista[(A, B)]): (Lista[A], Lista[B]) =
    (lst.map(_._1), lst.map(_._2))

  def distributeOpcion[A, B](opc: Opcion[(A, B)]): (Opcion[A], Opcion[B]) =
    (opc.map(_._1), opc.map(_._2))

  println(distributeLista(Lista(1 -> "one", 2 -> "two", 3 -> "three")))
}
