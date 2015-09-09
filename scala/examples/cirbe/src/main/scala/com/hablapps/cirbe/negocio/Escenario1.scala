package com.hablapps.cirbe.negocio

import scalaz._, Scalaz._

import com.hablapps.cirbe.proceso.crgope._, CRGOPE._, StateInterpreter._
import com.hablapps.cirbe.proceso.validacion._, Validacion._, Resultado._
import com.hablapps.cirbe.dominio._

object Negocio extends App {

  val v1: Validacion[DB020] =
    (Si [DB020] (_.tipoProducto esIgualA (V39, V40))
      Entonces  (_.tipoRiesgo esDistintoDe ZZZ)
      EnCasoContrario R2008)

  val v2: Validacion[DB020] =
    (Si [DB020] (_.tipoProducto esDistintoDe (V39, V48, V54))
      Entonces  (_.tipoRiesgo esIgualA ZZZ)
      EnCasoContrario R2009)

  implicit val vs: Validacion[DB020] = v1 && v2

  def declarar[R <: Registro { type This = R } : Validacion](
      registros: List[R]): ProgramaCRGOPE[EstadoDeclaracion] = for {
    referencias <- paraTodos(registros)(altaBorrador(_))
    resultados  <- paraTodas(referencias)(aplicarValidaciones(_))
  } yield toEstadoDeclaracion(resultados)

  val regs = List(DB020(V39, ZZZ), DB020(V48, ZZZ))
  println(declarar(regs).foldMap(toState).run((0, Map())))
}
