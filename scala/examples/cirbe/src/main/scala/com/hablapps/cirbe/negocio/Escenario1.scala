// package com.hablapps.cirbe.negocio
//
// import scalaz._, Scalaz._
//
// import com.hablapps.cirbe.proceso.crgope._, CRGOPE._, StateInterpreter._
// import com.hablapps.cirbe.proceso.validacion._, Validacion._, Resultado._
// import com.hablapps.cirbe.dominio._
//
// object Negocio extends App {
//
//   val v1: Validacion[DB020] =
//     (Si [DB020] (_.tipoProducto esIgualA (V39, V40))
//       Entonces  (_.tipoRiesgo esDistintoDe ZZZ)
//       EnCasoContrario R2008)
//
//   val v2: Validacion[DB020] =
//     (Si [DB020] (_.tipoProducto esDistintoDe (V39, V48, V54))
//       Entonces  (_.tipoRiesgo esIgualA ZZZ)
//       EnCasoContrario R2009)
//
//   implicit val _: Validacion[DB020] = v1 && v2
//
//   type EstadoBdE[A] = (List[(Ref[A], Resultado)], EstadoDeclaracion)
//
//   def declarar[R <: Registro { type This = R } : Validacion](
//       registros: List[R]): ProgramaCRGOPE[EstadoBdE[R]] = for {
//     referencias <- paraTodos(registros)(altaBorrador(_))
//     resultados  <- paraTodas(referencias)(aplicarValidaciones(_))
//   } yield (referencias.zip(resultados), toEstadoDeclaracion(resultados))
//
//   def emitir[R <: Registro { type This = R } : Validacion](
//       registros: List[R]): ProgramaCRGOPE[Unit] = for {
//     estadoBdE <- declarar(registros)
//     validadas = estadoBdE._1.filter(_._2.esValido).map(_._1)
//     _         <- enviar(validadas)
//     _         <- paraTodas(validadas)(actualizarEstado(_, Remitido))
//   } yield ()
//
//   def notificar[R <: Registro { type This = R }](
//       estadoBdE: EstadoBdE[R]): ProgramaCRGOPE[Unit] = {
//     val aceptadas  = estadoBdE._1.filter(_._2.esValido).map(_._1)
//     val rechazadas = estadoBdE._1.filter(! _._2.esValido).map(_._1)
//     val errores = estadoBdE._1.collect({
//       case (rr, Invalido(errs)) => (rr, Invalido(errs))
//     })
//     for {
//       _ <- paraTodas(aceptadas)(actualizarEstado(_, Aceptado))
//       _ <- paraTodas(rechazadas)(actualizarEstado(_, Rechazado))
//       _ <- paraTodas(aceptadas)(actualizarErrores(_, List.empty))
//       _ <- paraTodos(errores)(e => actualizarErrores(e._1, e._2.errores.toList))
//     } yield ()
//   }
//
//   val escenario: ProgramaCRGOPE[Unit] = for {
//     _ <- emitir(List(DB020(V39, ZZZ), DB020(V48, ZZZ)))
//     _ <- notificar((List((1: Ref[DB020], Valido)), AceptacionTotal))
//   } yield ()
//
//   val regs = List(DB020(V39, ZZZ), DB020(V48, ZZZ))
//   println(escenario.foldMap(toState).run((0, Map())))
// }
