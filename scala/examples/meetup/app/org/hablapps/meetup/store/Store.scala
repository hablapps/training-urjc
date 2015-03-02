package org.hablapps.meetup.db

import scala.reflect.{ClassTag, classTag}
import scalaz.{\/, -\/, \/-, Free, Monad, Coyoneda}

import org.hablapps.meetup.domain._

sealed trait StoreOp[+A]

object StoreOp {
  case class GetGroup(id: Int) extends StoreOp[Group]
  case class GetUser(id: Int) extends StoreOp[User]
  case class IsMember(uid: Int, gid: Int) extends StoreOp[Boolean]
  case class IsPending(uid: Int, gid: Int) extends StoreOp[Boolean]
  case class PutJoin(join: JoinRequest) extends StoreOp[JoinRequest]
  case class PutMember(member: Member) extends StoreOp[Member]
  case class Fail(error: StoreError) extends StoreOp[Nothing]
}

sealed class StoreError(val msg: String)

case class NonExistentEntity(id: Int) extends StoreError(s"Non-existent entity $id")
case class ConstraintFailed(constraint: StoreOp[Boolean]) extends StoreError(s"Constraint failed: $constraint")
case class GenericError(override val msg: String) extends StoreError(msg)

  
object Store {

  type Store[A] = Free.FreeC[StoreOp, A]
  implicit val MonadStore: Monad[Store] =
    Free.freeMonad[({type f[x] = Coyoneda[StoreOp, x]})#f]
  
  def cond[U,V](f: => Boolean, `then`: Store[V], `else`: Store[U]): Store[U \/ V] =
    if (f) 
      `then` map (u => \/-(u))
    else
      `else` map (v => -\/(v))

  def getGroup(id: Int) =
    Free.liftFC(StoreOp.GetGroup(id))
  
  def getUser(id: Int) =
    Free.liftFC(StoreOp.GetUser(id))

  def putJoin(t: JoinRequest) =
    Free.liftFC(StoreOp.PutJoin(t))

  def putMember(t: Member) =
    Free.liftFC(StoreOp.PutMember(t))

  def isMember(uid: Int, gid: Int) =
    Free.liftFC(StoreOp.IsMember(uid, gid))

  def isPending(uid: Int, gid: Int) =
    Free.liftFC(StoreOp.IsPending(uid, gid))

  implicit class StoreOps[U](store: Store[U]){

    def unless(violation: StoreOp[Boolean]): Store[U] = 
      Free.liftFC(violation) flatMap {
        violated => if (violated)
          Free.liftFC(StoreOp.Fail(ConstraintFailed(violation)))
        else 
          store
      }

    def ||(cond2: Store[Boolean])(implicit e: U=:=Boolean): Store[Boolean] = 
      store flatMap {
        bool1 => 
          if (bool1) MonadStore.point(true)
          else cond2
      }
  }

}