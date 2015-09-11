// package com.hablapps.cirbe.proceso.crgope
//
// import scalaz._, Scalaz._
//
// import CRGOPE._
// import com.hablapps.cirbe.dominio._
//
// object StateInterpreter {
//
//   type CRGOPEState = (Int, Map[Int, Any])
//
//   type StateCRGOPE[A] = State[CRGOPEState, A]
//
//   def toState = new (CRGOPE ~> StateCRGOPE) {
//     def apply[A](crgope: CRGOPE[A]): StateCRGOPE[A] = crgope match {
//       case Alta(a) => for {
//         c <- gets((s: CRGOPEState) => s._1)
//         _ <- modify((s: CRGOPEState) => (s._1 + 1, s._2 + (s._1 -> a)))
//       } yield c
//       case Evaluar(ra) => for {
//         m <- gets((s: CRGOPEState) => s._2)
//       } yield m(ra).asInstanceOf[A]
//       case Actualizar(ra, a) => for {
//         _ <- modify((s: CRGOPEState) => (s._1, s._2.updated(ra, a)))
//       } yield ()
//       case Validar(rr, v) => for {
//         m <- gets((s: CRGOPEState) => s._2)
//       } yield v.run(m(rr).asInstanceOf[Registro])
//       case Enviar(rs) => (implicitly[Monad[StateCRGOPE]].traverse(rs) { r =>
//         for {
//           v <- gets((s: CRGOPEState) => s._2(r))
//           _ = println(s"=> Enviando registro '$v' a BdE")
//         } yield ()
//       }) as (())
//     }
//   }
// }
