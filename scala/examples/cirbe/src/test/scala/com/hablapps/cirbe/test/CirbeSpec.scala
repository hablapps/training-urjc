package com.hablapps.cirbe.test

import org.scalatest._

import scalaz._, Scalaz._

import com.hablapps.cirbe.{ dominio, negocio, proceso }
import dominio._, dominio.Crgopes
import negocio.Validaciones._
import proceso.validacion._, proceso.crgopes._, proceso.crgopes.Crgopes._
import StateInterpreter._

class CirbeSpec extends FlatSpec with Matchers {

  val envio = "09-2015"

  val inicial = Estado(
    cirbe    = Cirbe(procesos = List(envio)),
    procesos = Map(envio -> Proceso(envio)),
    crgopes  = Map(envio -> Crgopes()))

  "Cirbe" should "comparar contra valores puntuales del estado" in {

    val programa = for {
      db020 <- declarar(DB020(Operacion("ABCD", V40, ZZZ)), envio)
      db010 <- declarar(DB010(Relacion("1234", "ABCD")), envio)
      _     <- validar(db010)
      _     <- validar(db020)
    } yield ()

    val resultado = programa.foldMap(toState).exec(inicial)

    resultado.db010s("DB010_1234_ABCD").errores shouldBe List()
    resultado.db020s("DB020_ABCD").errores shouldBe List(R2008)
  }

  it should "comparar contra estado completo" in {
    pending
  }
}
