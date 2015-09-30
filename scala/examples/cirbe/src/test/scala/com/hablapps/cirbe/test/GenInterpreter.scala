package com.hablapps.cirbe.test

import scalaz._, Scalaz._

import org.scalacheck._
import Gen._
import Arbitrary.arbitrary

import com.hablapps.cirbe.dominio._
import com.hablapps.cirbe.dominio.Id
import com.hablapps.cirbe.proceso.crgopes.Crgopes._

object GenInterpreter {

  case class Delta(
      procesos: List[Id[Proceso]] = List.empty,
      crgopes: List[Id[Crgopes]] = List.empty,
      registros: List[Id[Registro]] = List.empty) {
    def addProceso(pro: Proceso) = copy(procesos = pro.nombre +: procesos)
    def addCrgopes(crg: Crgopes) = copy(crgopes = crg.nombre +: crgopes)
    def addRegistro(reg: Registro) = copy(registros = reg.id +: registros)
  }

  implicit val genMonad = new Functor[Gen] with Monad[Gen] {
    def point[A](a: => A): Gen[A] = a
    def bind[A, B](fa: Gen[A])(f: A => Gen[B]): Gen[B] = fa flatMap f
  }

  implicit class listHelper[A](p: List[A]) {
    def condCat(cond: Boolean, q: List[A]): List[A] = if (cond) p ++ q else p
    def condCat(cond: Boolean, e: A): List[A] = condCat(cond, List(e))
  }

  val genProceso: Gen[Proceso] = for {
    nombre  <- arbitrary[String]
  } yield Proceso(nombre)

  implicit val arbitraryProceso: Arbitrary[Proceso] = Arbitrary(genProceso)

  val genCrgopes: Gen[Crgopes] = for {
    nombre <- arbitrary[String]
    // estado <- oneOf(Activo, Finalizado)
  } yield Crgopes(nombre, Activo) // TODO: añadir información estado a delta

  implicit val arbitraryCrgopes: Arbitrary[Crgopes] = Arbitrary(genCrgopes)

  val genRelacion: Gen[Relacion] = for {
    codigoOperacion <- arbitrary[String] suchThat (_.length > 4)
    codigoTitular   <- arbitrary[String] suchThat (_.length > 4)
  } yield Relacion(codigoOperacion, codigoTitular)

  implicit val arbitraryRelacion: Arbitrary[Relacion] = Arbitrary(genRelacion)

  val genOperacion: Gen[Operacion] = for {
    codigo       <- arbitrary[String] suchThat (_.length > 4)
    tipoProducto <- oneOf(V39, V40, V48, V54)
    tipoRiesgo   <- ZZZ
  } yield Operacion(codigo, tipoProducto, tipoRiesgo)

  implicit val arbitraryOperacion: Arbitrary[Operacion] = Arbitrary(genOperacion)

  val genDB010: Gen[DB010] = for {
    relacion <- arbitrary[Relacion]
  } yield DB010(relacion)

  val genDB020: Gen[DB020] = for {
    operacion <- arbitrary[Operacion]
  } yield DB020(operacion)

  val genRegistro: Gen[Registro] = oneOf(genDB010, genDB020)

  implicit class ToGenHelper[A](programa: List[Precond]) {

    val m  = Monad[Gen]
    val m2 = Monad[ProgramaCrgopes]

    def toGen: Gen[ProgramaCrgopes[Unit]] = {

      type P[A]  = ProgramaCrgopes[A]
      type GP[A] = Gen[P[A]]

      def unir[A, B]: (GP[A], GP[B]) => GP[Unit] =
        m.lift2[P[A], P[B], P[Unit]]((pa, pb) => m2.apply2(pa, pb)((a, b) => ()))

      def toG(prog: List[Precond], delta: Delta): GP[Unit] = {

        val gens: List[GP[Unit]] =
          // Siempre es posible crear procesos
          List(genProceso.flatMap { p =>
            unir(
              crearProceso(p),
              toG(prog, delta.addProceso(p)))
          })
          // Existen procesos, luego podemos crear 'crgopes'
          .condCat(delta.procesos.nonEmpty, genCrgopes flatMap { crg =>
            oneOf(delta.procesos) flatMap { pro_id =>
              unir(
                crearCrgopes(crg, pro_id),
                toG(prog, delta.addCrgopes(crg)))
            }
          })
          // Existen crgopes, luego podemos crear 'db010s' o 'db020s'
          .condCat(delta.crgopes.nonEmpty, genRegistro flatMap { reg =>
            oneOf(delta.crgopes) flatMap {crg_id =>
              unir(
                putRegistro(reg, crg_id),
                toG(prog, delta.addRegistro(reg)))
            }
          })
          // Se cumple la precondición del usuario
          .condCat(true, List.empty)
          // Se cumplen todas las precondiciones, por tanto podemos finalizar
          .condCat(prog.isEmpty, returns(()))

        // FIXME: no es 'safe', va a cascar con 50% de probabilidad!
        oneOf(gens.head, gens.tail.head, gens.tail.tail:_*)
      }

      toG(programa, Delta())
    }
  }
}
