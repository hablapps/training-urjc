package org.hablapps.meetup.db

import scala.reflect.{ClassTag, classTag}
import org.hablapps.meetup.domain._

object MapInterpreter{

  sealed abstract class MapError(val msg: String)
  
  case class WrongType(id: Int) extends MapError(
    s"Entity $id exists but its type is not right")

  case class NonExistentId(id: Int) extends MapError(s"Non-existent entity $id")

  case class MapStore(map: Map[Int, Any], next: Int){
    def get[T: ClassTag](id: Int): Either[MapError, T] = 
      map.get(id).fold[Either[MapError,T]](Left(NonExistentId(id))){
        entity => if (!classTag[T].runtimeClass.isInstance(entity))
          Left(WrongType(id))
        else 
          Right(entity.asInstanceOf[T])
      }
  }

  object MapStore{
    def apply(entities: Any*): MapStore = 
      MapStore((1.to(entities.size) zip entities).toMap, entities.size+1)
  }

  def output[U](store: MapStore): Store[U] => Either[StoreError, U] = 
    storeProgram => run(storeProgram)(store)._2
  
  def run[U](storeProgram: Store[U])(store: MapStore): (MapStore, Either[StoreError, U]) = 
    storeProgram match {
      case i@Return(result) =>
        println(s"--> $i") 
        (store, Right(result))
      case i@Fail(error) => 
        println(s"--> $i") 
        (store, Left(error))
      case GetUser(id, next) => 
        println(s"--> GetUser($id)")
        store.get[User](id).fold(
          error => (store, Left(error match { 
            case e@ WrongType(id) => GenericError(s"Exists an entity with id $id, but it's not a user")
            case e@ NonExistentId(id) => NonExistentEntity(id) 
          })),
          user => run(next(user))(store)
        )
      case IsMember(uid, gid, next) => 
        println(s"--> IsMember($uid, $gid)")
        val satisfied = !store.map.filter{ 
          case (_, Member(_, uid, gid)) => true
          case _ => false 
        }.isEmpty
        run(next(satisfied))(store)
      case IsPending(uid, gid, next) => 
        println(s"--> IsPending($uid, $gid)")
        val satisfied = !store.map.filter{ 
          case (_, JoinRequest(_, uid, gid)) => true
          case _ => false 
        }.isEmpty
        run(next(satisfied))(store)
      case GetGroup(id, next) => 
        println(s"--> GetGroup($id)")
        store.get[Group](id).fold(
          error => (store, Left(error match { 
            case e@ WrongType(id) => GenericError(s"Exists an entity with id $id, but it's not a group")
            case e@ NonExistentId(id) => NonExistentEntity(id) 
          })),
          group => run(next(group))(store)
        )
      case PutMember(member, next) => 
        println(s"--> PutMember($member)")
        val memberWithId = member.copy(mid = Some(store.next))
        val newStore = MapStore(store.map + (store.next -> memberWithId), store.next + 1)
        run(next(memberWithId))(newStore)
      case PutJoin(join, next) => 
        println(s"--> PutJoin($join)")
        val joinWithId = join.copy(jid = Some(store.next))
        val newStore = MapStore(store.map + (store.next -> joinWithId), store.next + 1)
        run(next(joinWithId))(newStore)
      case storeP => 
        (store, Left(GenericError(s"$storeP uninterpreted")))
    }

}
