package com.hablapps.cirbe.proceso.validacion

import scala.language.implicitConversions

import scalaz._, Scalaz._

import com.hablapps.cirbe.dominio._

trait Resultado {

  def esValido: Boolean

  def &&(r: Resultado): Resultado = (this, r) match {
    case (Valido, r2) => r2
    case (r2, Valido) => r2
    case (Invalido(err1), Invalido(err2)) => Invalido(err1 append err2)
  }
}

object Resultado {

  implicit def resultadoMonoid = new Monoid[Resultado] {

    def zero = Valido

    def append(r1: Resultado, r2: => Resultado) = r1 && r2
  }

  // def toEstadoDeclaracion(es: List[Resultado]): EstadoDeclaracion =
  //   es.filter(_.esValido).size match {
  //     case n if n == es.size => AceptacionTotal
  //     case 0 => RechazoTotal
  //     case _ => AceptacionParcial
  //   }
}

case object Valido extends Resultado {
  def esValido = true
}

case class Invalido(errores: NonEmptyList[Error]) extends Resultado {
  def esValido = false
}

case class Validacion[A](run: A => Resultado) {
  def &&(v: Validacion[A]): Validacion[A] = Validacion { a =>
    run(a) && v.run(a)
  }
}

object Validacion {

  implicit def validacionMonoid[A] = new Monoid[Validacion[A]] {

    def zero = Validacion[A](_ => Valido)

    def append(v1: Validacion[A], v2: => Validacion[A]) = v1 && v2
  }

  implicit class EsIgualAHelper[A](a: A) {

    def esIgualA(as: A*): Boolean = as.contains(a)

    def esDistintoDe(as: A*): Boolean = ! esIgualA(as: _*)
  }
}

case class SiBuilder[A](
    condicion: Option[A => Boolean] = None,
    entonces: Option[A => Boolean]  = None,
    enCasoContrario: Option[Error]  = None) {

  def Condicion(f: A => Boolean): SiBuilder[A] =
    copy(condicion = Some(f))

  def Entonces(f: A => Boolean): SiBuilder[A] =
    copy(entonces = Some(f))

  def EnCasoContrario(err: Error): SiBuilder[A] =
    copy(enCasoContrario = Option(err))
}

object SiBuilder {
  implicit def toValidacion[A](builder: SiBuilder[A]) = Validacion[A] { a =>
    (builder.condicion.get(a), builder.entonces.get(a)) match {
      case (false, _) | (true, true) => Valido
      case (true, false) => Invalido(NonEmptyList(builder.enCasoContrario.get))
    }
  }
}

object Si {
  def apply[A](condicion: A => Boolean): SiBuilder[A] =
    SiBuilder(condicion = Option(condicion))
}
