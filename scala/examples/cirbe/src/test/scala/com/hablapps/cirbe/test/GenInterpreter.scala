package com.hablapps.cirbe.test

import scala.language.implicitConversions

import scalaz._, Scalaz._

import org.scalacheck._
import Gen._
import Arbitrary.arbitrary

import com.hablapps.cirbe.dominio._
import com.hablapps.cirbe.dominio.Id
import com.hablapps.cirbe.proceso.crgopes.Crgopes._

object GenInterpreter {

  implicit val genMonad = new Functor[Gen] with Monad[Gen] {
    def point[A](a: => A): Gen[A] = a
    def bind[A, B](fa: Gen[A])(f: A => Gen[B]): Gen[B] = fa flatMap f
  }

  implicit class listHelper[A](p: List[A]) {
    def condCat(cond: Boolean, e: => A): List[A] = if (cond) e :: p else p
  }

  implicit def unirPreconds[A](ps: List[A => Boolean]): A => Boolean =
    a => ps.foldLeft(true)((acc, p) => acc && p(a))

  val genProceso: Gen[Proceso] = for {
    nombre  <- arbitrary[String]
  } yield Proceso(nombre)

  implicit val arbitraryProceso: Arbitrary[Proceso] = Arbitrary(genProceso)

  val genCrgopes: Gen[Crgopes] = for {
    nombre <- arbitrary[String]
    estado <- oneOf(Activo, Finalizado)
  } yield Crgopes(nombre, estado)

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
          // Se cumplen la precondiciones del usuario
          .condCat(
            prog.headOption.fold(false)(_.cumple(delta)),
            prog.head match {
              case RequiereProceso(nom, pre) => {
                genProceso.suchThat(pre) flatMap { pro =>
                  val pro2 = pro.copy(nombre = nom.getOrElse(pro.nombre))
                  unir(
                    crearProceso(pro2),
                    toG(prog.tail, delta.addProceso(pro2)))
                }
              }
              case RequiereCrgopes(nom, ctx, pre) => {
                genCrgopes.suchThat(pre) flatMap { crg =>
                  oneOf(delta.procesos) flatMap { pro_id =>
                    val crg2 = crg.copy(nombre = nom.getOrElse(crg.nombre))
                    unir(
                      crearCrgopes(crg2, ctx.getOrElse(pro_id)),
                      toG(prog.tail, delta.addCrgopes(crg2)))
                  }
                }
              }
              case RequiereDB010(nom, ctx, pre) => {
                genDB010.suchThat(pre) flatMap { db010 =>
                  oneOf(delta.crgopes) flatMap { crg_id =>
                    val db010_2 = db010.copy(nombre = nom.getOrElse(db010.nombre))
                    unir(
                      putRegistro(db010_2, ctx.getOrElse(crg_id)),
                      toG(prog.tail, delta.addRegistro(db010_2)))
                  }
                }
              }
              case RequiereDB020(nom, ctx, pre) => {
                genDB020.suchThat(pre) flatMap { db020 =>
                  oneOf(delta.crgopes) flatMap { crg_id =>
                    val db020_2 = db020.copy(nombre = nom.getOrElse(db020.nombre))
                    unir(
                      putRegistro(db020_2, ctx.getOrElse(crg_id)),
                      toG(prog.tail, delta.addRegistro(db020_2)))
                  }
                }
              }
            }
          )
          // Se cumplen todas las precondiciones, por tanto podemos finalizar
          .condCat(prog.isEmpty, returns(()))

        gens match {
          case (x1::x2::xs) => oneOf(x1, x2, xs:_*)
          case (x1::_) => x1
          case _ => throw new Exception("Siempre tendremos al menos un generador")
        }
      }

      toG(programa, Delta())
    }
  }
}
