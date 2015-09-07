package com.hablapps.cirbe.negocio

import com.hablapps.cirbe.proceso.validacion._, Validacion._
import com.hablapps.cirbe.dominio._

object Negocio extends App {

  // El valor de “Tipo de riesgo” es distinto de ZZZ si “Tipo de producto” es
  // igual a V39 o V48. En caso contrario se rechaza el registro con código de
  // situación R2008.
  (Si [DB020] (_.tipoProducto esIgualA (V39, V40))
    Entonces  (_.tipoRiesgo esDistintoDe ZZZ)
    EnCasoContrario R2008)

  // El valor de “Tipo de riesgo asociado a los derivados” es ZZZ si “Tipo de
  // producto” es distinto de V39, V48, V54. En caso contrario se rechaza el
  // registro con código de situación R2009.
  (Si [DB020] (_.tipoProducto esDistintoDe (V39, V48, V54))
    Entonces  (_.tipoRiesgo esIgualA ZZZ)
    EnCasoContrario R2009)
}
