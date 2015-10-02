package com.hablapps.cirbe.negocio

import scalaz._, Scalaz._

import com.hablapps.cirbe.dominio._
import com.hablapps.cirbe.proceso.validacion._, Validacion._
import com.hablapps.cirbe.proceso.crgopes.Crgopes._

object Validaciones {

  val v1: Validacion[DB020] =
    (Si [DB020] (_.operacion.tipoProducto esIgualA (V39, V40))
      Entonces  (_.operacion.tipoRiesgo esDistintoDe ZZZ)
      EnCasoContrario R2008)

  val v2: Validacion[DB020] =
    (Si [DB020] (_.operacion.tipoProducto esDistintoDe (V39, V48, V54))
      Entonces  (_.operacion.tipoRiesgo esIgualA ZZZ)
      EnCasoContrario R2009)

  implicit val vDB020: Validacion[DB020] = v1 && v2
}
