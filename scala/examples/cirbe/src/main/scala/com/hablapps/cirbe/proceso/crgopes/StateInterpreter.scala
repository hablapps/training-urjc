package com.hablapps.cirbe.proceso.crgopes

import scala.reflect.runtime.universe._

import scalaz._, Scalaz._

import Crgopes._
import com.hablapps.cirbe.dominio._
import com.hablapps.cirbe.dominio.Id
import com.hablapps.cirbe.proceso.validacion._

object StateInterpreter {

  case class Estado(
      cirbe:    Cirbe = Cirbe(),
      procesos: Map[Id[Proceso], Proceso] = Map(),
      crgopes:  Map[Id[Crgopes], Crgopes] = Map(),
      db010s:   Map[Id[DB010], DB010] = Map(),
      db020s:   Map[Id[DB020], DB020] = Map()) {

    def addDB010(db010: DB010, crgopes_id: Id[Crgopes]): Estado = {
      val old = crgopes(crgopes_id)
      val fresh = old.copy(db010s = db010.id +: old.db010s)
      copy(
        db010s = db010s + (db010.id -> db010),
        crgopes = crgopes.updated(crgopes_id, fresh))
    }

    def addDB020(db020: DB020, crgopes_id: Id[Crgopes]): Estado = {
      val old = crgopes(crgopes_id)
      val fresh = old.copy(db020s = db020.id +: old.db020s)
      copy(
        db020s = db020s + (db020.id -> db020),
        crgopes = crgopes.updated(crgopes_id, fresh))
    }

    def finalizar(crgopes_id: Id[Crgopes]): Estado = {
      copy(crgopes = crgopes.updated(
        crgopes_id,
        crgopes(crgopes_id).copy(estado = Finalizado)))
    }
  }

  type StateCirbe[A] = State[Estado, A]
  val monad = implicitly[Monad[StateCirbe]]

  def toState = new (InstruccionCrgopes ~> StateCirbe) {
    def apply[A](ins: InstruccionCrgopes[A]): StateCirbe[A] = ins match {
      case PutRegistro(registro, crgopes_id) => for {
        estado <- (get: StateCirbe[Estado])
        estado2 = registro match {
          case (r: DB010) => estado.addDB010(r, crgopes_id)
          case (r: DB020) => estado.addDB020(r, crgopes_id)
          case (_: Finalizar) => estado.finalizar(crgopes_id)
        }
        _ <- put(estado2)
      } yield registro.id
      case Fallar(mensaje) => throw new Error(mensaje)
      case GetCrgopes(crgopes_id) => for {
        estado <- (get: StateCirbe[Estado])
      } yield estado.crgopes.get(crgopes_id)
      case ins@GetRegistro(registro_id) => registro_id match {
        case (id: Id[DB010]) if ins.typeTag.tpe <:< typeOf[DB010] => {
          get.map(_.db010s(registro_id).asInstanceOf[A])
        }
        case (id: Id[DB020]) if ins.typeTag.tpe <:< typeOf[DB020] => {
          get.map(_.db020s(registro_id).asInstanceOf[A])
        }
      }
      case Remitir(rs) => {
        println("Remitiendo registros")
        rs.foreach(r => println(s"cirbe> Remitiendo registro '$r' a BdE"))
        monad.point(())
      }
      case SolicitarConfirmacion(_) => monad.point(true)
      case ins@Validar(r, v) => r match {
        case (r: Id[DB010]) if ins.typeTag.tpe <:< typeOf[DB010] => for {
          estado <- (get: StateCirbe[Estado])
          registro = estado.db010s(r)
          resultado = v.run(registro)
          _ <- resultado match {
            case Valido => monad.point(())
            case Invalido(errs) => {
              val registro2 = registro.copy(errores = errs.toList)
              val estado2 = estado.copy(
                db010s = estado.db010s.updated(r, registro2))
              put(estado2)
            }
          }
        } yield resultado
        case (r: Id[DB020]) if ins.typeTag.tpe <:< typeOf[DB020] => for {
          estado <- (get: StateCirbe[Estado])
          registro = estado.db020s(r)
          resultado = v.run(registro)
          _ <- resultado match {
            case Valido => monad.point(())
            case Invalido(errs) => {
              val registro2 = registro.copy(errores = errs.toList)
              val estado2 = estado.copy(
                db020s = estado.db020s.updated(r, registro2))
              put(estado2)
            }
          }
        } yield resultado
      }
    }
  }

  implicit class remove[A](xs: List[A]) {
    def remove(a: A) = xs.diff(List(a))
  }
}
