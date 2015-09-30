package com.hablapps.cirbe.test

import org.scalacheck._
import org.scalacheck.Properties
import org.scalacheck.Prop.{ forAll, BooleanOperators }

import scalaz._, Scalaz._

import com.hablapps.cirbe.{ dominio, negocio, proceso }
import dominio._, dominio.{ Crgopes, Id }
import negocio.Validaciones._
import proceso.validacion._, proceso.crgopes._, proceso.crgopes.Crgopes._
import proceso.crgopes.Crgopes.test
import StateInterpreter._
import GenInterpreter._

object CirbeCheck extends Properties("Cirbe") {

  property("fallar con declaración finalizada") = {

    val myGen: Gen[ProgramaCrgopes[Unit]] = List(
      RequiereProceso(nombre = Option("nuevo_proceso")),
      RequiereCrgopes(
        nombre   = Option("nuevo_crgopes"),
        context  = Option("nuevo_proceso"),
        preconds = List(_.estado == Finalizado))).toGen

    forAll(myGen) { inicial =>

      val programa = for {
        _ <- inicial
        _ <- declarar(DB020(Operacion("ABCD", V40, ZZZ)), "nuevo_crgops")
      } yield ()

      programa.foldMap(toState).exec(Estado()).isLeft
    }
  }
}
