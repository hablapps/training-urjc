package org.hablapps.meetup.db

import scala.reflect.{ClassTag, classTag}
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.lifted.CompiledFunction
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import scalaz.{\/, -\/, \/-}

import play.api.db.slick.DB
import play.api.Play.current

import org.hablapps.meetup.domain._

object MySQLInterpreter{

  def run[U](store: Store[U]): StoreError \/ U = store match {
    case Return(result) => 
      \/-(result)
    case Fail(error) => 
      -\/(error)
    case GetGroup(id: Int, next: (Group => Store[U])) => 
      DB.withSession { implicit session =>
        group_table.byID(Some(id)).firstOption.fold[StoreError \/ U](
          -\/(NonExistentEntity(id))
        ){
          entity => run(next(entity))
        }
      }
    case GetUser(id: Int, next: (User => Store[U])) => 
      DB.withSession { implicit session =>
        user_table.byID(Some(id)).firstOption.fold[StoreError \/ U](
          -\/(NonExistentEntity(id))
        ){
          entity => run(next(entity))
        }
      }
    case IsMember(uid, gid, next) => 
      DB.withSession { implicit session =>
        val isMemberQuery = for{
          member <- member_table if member.uid === uid && member.gid === gid
        } yield member
        run(next(isMemberQuery.firstOption.isDefined))
      }
    case IsPending(uid, gid, next) => 
      DB.withSession { implicit session =>
        val isPendingQuery = for{
          join <- join_table if join.uid === uid && join.gid === gid
        } yield join
        run(next(isPendingQuery.firstOption.isDefined))
      }
    case PutMember(member: Member, next: (Member => Store[U])) => 
      DB.withSession { implicit session =>
        val mid: StoreError \/ Int = try{
          val maybeId = member_table returning member_table.map(_.mid) += member
          maybeId.fold[StoreError \/ Int](-\/(GenericError(s"Could not put new member $member")))(\/-(_))
        } catch {
          case e : MySQLIntegrityConstraintViolationException => 
            -\/(ConstraintFailed(Store.isMember(member.uid, member.gid)))
        }
        mid.map(id => run(next(member.copy(mid = Some(id)))))
          .flatMap(a => a)
      }
    case PutJoin(join: JoinRequest, next: (JoinRequest => Store[U])) => 
      DB.withSession { implicit session =>
        val jid: StoreError \/ Int = try{
          val maybeId = join_table returning join_table.map(_.jid) += join
          maybeId.fold[StoreError \/ Int](
            -\/(GenericError(s"Could not put new join $join")))(
            \/-(_)
          )
        } catch {
          case e : MySQLIntegrityConstraintViolationException => 
            -\/(GenericError(s"Constraint violation: $e"))
        }
        jid.map(id => run(next(join.copy(jid = Some(id)))))
          .flatMap(a => a)
      }
    case _ => 
      -\/(new StoreError("unevaluated yet"))
  }

  class GroupTable(tag: Tag) extends Table[Group](tag, "Groups") {
    def gid = column[Option[Int]]("gid", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def city = column[String]("city")
    def must_approve = column[Boolean]("must_approve")
    def * = (gid, name, city, must_approve) <> (Group.tupled, Group.unapply)
  }
  
  object group_table extends TableQuery[GroupTable](new GroupTable(_)){
    val byID = this.findBy(_.gid)
  }

  class UserTable(tag: Tag) extends Table[User](tag, "Users") {
    def uid = column[Option[Int]]("uid", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (uid, name) <> (User.tupled, User.unapply)
  }
  
  object user_table extends TableQuery[UserTable](new UserTable(_)){
    val byID = this.findBy(_.uid)
  }

  class MemberTable(tag: Tag) extends Table[Member](tag, "Members") {
    def mid = column[Option[Int]]("mid", O.PrimaryKey, O.AutoInc)
    def uid = column[Int]("uid")
    def gid = column[Int]("gid")
    def * = (mid, uid, gid) <> (Member.tupled, Member.unapply)
  }
  
  val member_table = TableQuery[MemberTable]

  class JoinTable(tag: Tag) extends Table[JoinRequest](tag, "Joins") {
    def jid = column[Option[Int]]("jid", O.PrimaryKey, O.AutoInc)
    def uid = column[Int]("uid")
    def gid = column[Int]("gid")
    def * = (jid, uid, gid) <> (JoinRequest.tupled, JoinRequest.unapply)
  }
  
  val join_table = TableQuery[JoinTable]

}