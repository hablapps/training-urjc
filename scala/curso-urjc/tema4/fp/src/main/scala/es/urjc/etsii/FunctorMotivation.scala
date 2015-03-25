package es.urjc.etsii

import scala.language.higherKinds

import es.urjc.etsii.adt._
import es.urjc.etsii.typeclass._

object FunctorMotivation extends App {

  def distributeLista[A, B](lst: Lista[(A, B)]): (Lista[A], Lista[B]) =
    (lst.map(tpl => tpl._1), lst.map(tpl => tpl._2))

  def distributeOpcion[A, B](opc: Opcion[(A, B)]): (Opcion[A], Opcion[B]) =
    (opc.map(tpl => tpl._1), opc.map(tpl => tpl._2))

  // println(distributeLista(Lista((1, "one"), (2, "two"))))
  // println(distributeOpcion(Algun((1, "one"))))
  // println(distributeOpcion(Ninguno))

  //def distribute[F[_]: Functor, A, B](arg: F[(A, B)]): (F[A], F[B]) = {
  // def distribute[F[_], A, B](arg: F[(A, B)])(implicit functor: Functor[F]): (F[A], F[B]) =
  //   (functor.map(arg)(tpl => tpl._1), functor.map(arg)(tpl => tpl._2))

  println(implicitly[Functor[Lista]].replace(Lista(1, 2, 3, 4, 5), 0))
  println(implicitly[Functor[Opcion]].replace(Algun("xxx"), 0))

}
