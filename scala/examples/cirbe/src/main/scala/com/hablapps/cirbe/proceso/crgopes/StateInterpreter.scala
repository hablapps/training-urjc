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

    def addProceso(proceso: Proceso): Estado = {
      val id: Id[Proceso] = proceso.nombre
      copy(
        procesos = procesos + (id -> proceso),
        cirbe = cirbe.copy(procesos = id :: cirbe.procesos))
    }

    def setCrgopes(value: Crgopes, proceso_id: Id[Proceso]): Estado = {
      val id: Id[Crgopes] = value.nombre
      val old = procesos(proceso_id)
      val fresh = old.copy(crgopes = Option(id))
      copy(
        crgopes = crgopes + (id -> value),
        procesos = procesos.updated(proceso_id, fresh))
    }

    def addDB010(db010: DB010, crgopes_id: Id[Crgopes]): Estado = {
      val old = crgopes(crgopes_id)
      val fresh = old.copy(db010s = db010.nombre +: old.db010s)
      copy(
        db010s = db010s + (db010.nombre -> db010),
        crgopes = crgopes.updated(crgopes_id, fresh))
    }

    def addDB020(db020: DB020, crgopes_id: Id[Crgopes]): Estado = {
      val old = crgopes(crgopes_id)
      val fresh = old.copy(db020s = db020.nombre +: old.db020s)
      copy(
        db020s = db020s + (db020.nombre -> db020),
        crgopes = crgopes.updated(crgopes_id, fresh))
    }

    def finalizar(crgopes_id: Id[Crgopes]): Estado = {
      copy(crgopes = crgopes.updated(
        crgopes_id,
        crgopes(crgopes_id).copy(estado = Finalizado)))
    }
  }

  type StateCirbe[A] = State[Estado, A]
  type EitherString[A] = String \/ A
  type StateXCirbe[F[_], A] = StateT[F, Estado, A]
  type StateErrorCirbe[A] = StateXCirbe[EitherString, A]
  val m =  implicitly[Monad[StateErrorCirbe]]
  val mt = implicitly[MonadTrans[StateXCirbe]]

  def toState = new (InstruccionCrgopes ~> StateErrorCirbe) {
    def apply[A](ins: InstruccionCrgopes[A]): StateErrorCirbe[A] = ins match {
      case CrearProceso(proceso) => for {
        estado <- get[Estado].lift[EitherString]
        _ <- put(estado.addProceso(proceso)).lift[EitherString]
      } yield proceso.nombre
      case CrearCrgopes(crgopes, proceso_id) => for {
        estado <- get[Estado].lift[EitherString]
        _ <- put(estado.setCrgopes(crgopes, proceso_id)).lift[EitherString]
      } yield crgopes.nombre
      case PutRegistro(registro, crgopes_id) => for {
        estado <- get[Estado].lift[EitherString]
        estado2 = registro match {
          case (r: DB010) => estado.addDB010(r, crgopes_id)
          case (r: DB020) => estado.addDB020(r, crgopes_id)
          case (_: Finalizar) => estado.finalizar(crgopes_id)
        }
        _ <- put(estado2).lift[EitherString]
      } yield registro.nombre
      case Fallar(mensaje) => mt.liftMU[EitherString[A]](mensaje.left)
      case GetCrgopes(crgopes_id) => for {
        estado <- get[Estado].lift[EitherString]
      } yield estado.crgopes.get(crgopes_id)
      case ins@GetRegistro(registro_id) => registro_id match {
        case (id: Id[DB010]) if ins.typeTag.tpe <:< typeOf[DB010] => {
          get.lift[EitherString].map(_.db010s(registro_id).asInstanceOf[A])
        }
        case (id: Id[DB020]) if ins.typeTag.tpe <:< typeOf[DB020] => {
          get.lift[EitherString].map(_.db020s(registro_id).asInstanceOf[A])
        }
      }
      case Remitir(rs) => {
        rs.foreach(r => println(s"cirbe> Remitiendo registro '$r' a BdE"))
        m.point(())
      }
      case SolicitarConfirmacion(_) => m.point(true)
      case ins@Validar(r, v) => r match {
        case (r: Id[DB010]) if ins.typeTag.tpe <:< typeOf[DB010] => for {
          estado <- get[Estado].lift[EitherString]
          registro = estado.db010s(r)
          resultado = v.run(registro)
          _ <- resultado match {
            case Valido => m.point(())
            case Invalido(errs) => {
              val registro2 = registro.copy(errores = errs.toList)
              val estado2 = estado.copy(
                db010s = estado.db010s.updated(r, registro2))
              put(estado2).lift[EitherString]
            }
          }
        } yield resultado
        case (r: Id[DB020]) if ins.typeTag.tpe <:< typeOf[DB020] => for {
          estado <- get[Estado].lift[EitherString]
          registro = estado.db020s(r)
          resultado = v.run(registro)
          _ <- resultado match {
            case Valido => m.point(())
            case Invalido(errs) => {
              val registro2 = registro.copy(errores = errs.toList)
              val estado2 = estado.copy(
                db020s = estado.db020s.updated(r, registro2))
              put(estado2).lift[EitherString]
            }
          }
        } yield resultado
      }
    }
  }
}
