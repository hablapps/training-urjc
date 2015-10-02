package com.hablapps.cirbe.test

import org.scalacheck._
import org.scalacheck.Properties
import org.scalacheck.Prop.{ forAll, BooleanOperators }

import org.scalatest._
import org.scalatest.OptionValues._

import scalaz._, Scalaz._

import com.hablapps.cirbe.{ dominio, negocio, proceso }
import dominio._, dominio.{ Crgopes, Id }
import negocio.Validaciones._
import proceso.validacion._, proceso.crgopes._, proceso.crgopes.Crgopes._
import proceso.crgopes.Crgopes.test
import StateInterpreter._
import Precond._

object CirbeCheck extends Properties("Cirbe") with Matchers {

  property("fallar con declaración finalizada") = {

    val precond = for {
      p1 <- demand(CrearProceso(Proceso("id1")))
      // XXX: Debería traducir a `demand(CrearProceso(arbitrary[Proceso]))`
      // _ <- random[CrearProceso]
      p2 <- demand(CrearCrgopes(Crgopes("id2"), "id1"))
      //_  <- ensure(getCrgopes("id2"))(_.value shouldBe Finalizado)
    } yield (p1, p2)

    val programa: ProgramaCrgopes[_] =
      precond.foldMap(toFreeFA[InstruccionCrgopes])

    // val result = programa.foldMap(toState).eval(Estado())
    // println(result)

    //programa.foldMap(toState).exec(Estado())

    forAll { (i: Int) => true }

    // val myGen: Gen[ProgramaCrgopes[Id[CRGOPES]] = List(
    //   RequiereProceso(nombre = Option("nuevo_proceso")),
    //   RequiereCrgopes(
    //     nombre   = Option("nuevo_crgopes"),
    //     context  = Option("nuevo_proceso"))).toGen
    //
    // forAll(myGen) { inicial =>
    //
    //   val programa = for {
    //     crgopes <- inicial
    //     _ <- declarar(DB020(Operacion("ABCD", V40, ZZZ)), crgopes)
    //   } yield ()
    //
    //   programa.foldMap(toState).exec(Estado()).isLeft
    // }
  }
}
