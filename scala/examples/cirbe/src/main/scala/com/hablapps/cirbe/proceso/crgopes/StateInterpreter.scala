package com.hablapps.cirbe.proceso.crgopes

import scala.language.higherKinds
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
  type ErrorString[A] = String \/ A
  type StateXCirbe[F[_], B] = StateT[F, Estado, B]
  type StateErrorCirbe[A] = StateXCirbe[ErrorString, A]
  val m = implicitly[Monad[StateErrorCirbe]]
  val mt = implicitly[MonadTrans[StateXCirbe]]

  def toState = new (InstruccionCrgopes ~> StateErrorCirbe) {
    def apply[A](ins: InstruccionCrgopes[A]): StateErrorCirbe[A] = ins match {
      case PutRegistro(registro, crgopes_id) => for {
        estado <- get[Estado].lift[ErrorString]
        estado2 = registro match {
          case (r: DB010) => estado.addDB010(r, crgopes_id)
          case (r: DB020) => estado.addDB020(r, crgopes_id)
          case (_: Finalizar) => estado.finalizar(crgopes_id)
        }
        _ <- put(estado2).lift[ErrorString]
      } yield registro.id
      case Fallar(mensaje) => {
        mt.liftMU(mensaje.left).asInstanceOf[StateErrorCirbe[A]]
      }
      case GetCrgopes(crgopes_id) => for {
        estado <- get[Estado].lift[ErrorString]
      } yield estado.crgopes.get(crgopes_id)
      case ins@GetRegistro(registro_id) => registro_id match {
        case (id: Id[DB010]) if ins.typeTag.tpe <:< typeOf[DB010] => {
          get.lift[ErrorString].map(_.db010s(registro_id).asInstanceOf[A])
        }
        case (id: Id[DB020]) if ins.typeTag.tpe <:< typeOf[DB020] => {
          get.lift[ErrorString].map(_.db020s(registro_id).asInstanceOf[A])
        }
      }
      case Remitir(rs) => {
        rs.foreach(r => println(s"cirbe> Remitiendo registro '$r' a BdE"))
        m.point(())
      }
      case SolicitarConfirmacion(_) => m.point(true)
      case ins@Validar(r, v) => r match {
        case (r: Id[DB010]) if ins.typeTag.tpe <:< typeOf[DB010] => for {
          estado <- get[Estado].lift[ErrorString]
          registro = estado.db010s(r)
          resultado = v.run(registro)
          _ <- resultado match {
            case Valido => m.point(())
            case Invalido(errs) => {
              val registro2 = registro.copy(errores = errs.toList)
              val estado2 = estado.copy(
                db010s = estado.db010s.updated(r, registro2))
              put(estado2).lift[ErrorString]
            }
          }
        } yield resultado
        case (r: Id[DB020]) if ins.typeTag.tpe <:< typeOf[DB020] => for {
          estado <- get[Estado].lift[ErrorString]
          registro = estado.db020s(r)
          resultado = v.run(registro)
          _ <- resultado match {
            case Valido => m.point(())
            case Invalido(errs) => {
              val registro2 = registro.copy(errores = errs.toList)
              val estado2 = estado.copy(
                db020s = estado.db020s.updated(r, registro2))
              put(estado2).lift[ErrorString]
            }
          }
        } yield resultado
      }
    }
  }
}
