package org.hablapps.meetup.logic

import scalaz.{\/, -\/, \/-}
import play.api.Play.current
import play.api.db.slick.DB

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import scala.slick.driver.MySQLDriver.simple._
import org.hablapps.meetup.domain._
import org.hablapps.meetup.db._, MySQLInterpreter._

object SemiImpureScalaz {
  
  trait Store{
    def getGroup(gid: Int): StoreError \/ Group
    def getUser(uid: Int): StoreError \/ User
    def putJoin(join: JoinRequest): StoreError \/ JoinRequest
    def putMember(member: Member): StoreError \/ Member
    def isMember(uid: Int, gid: Int): Boolean
    def isPending(uid: Int, gid: Int): Boolean
  }

  object MySQLStore extends Store{

    def getGroup(gid: Int): StoreError \/ Group = 
      DB.withSession { implicit session =>
        val maybeGroup = (for { 
          group <- group_table if group.gid === gid
        } yield group).firstOption
        maybeGroup.fold[StoreError \/Group](
          -\/(NonExistentEntity(gid)))(
          \/-(_))
      }
     
    def getUser(uid: Int): StoreError \/ User =  
      DB.withSession { implicit session =>
        val maybeUser = (for {
          user <- user_table if user.uid === uid
        } yield user).firstOption
        maybeUser.fold[StoreError \/User](
          -\/(NonExistentEntity(uid)))(
          \/-(_))
      }
      
    def putJoin(join: JoinRequest): StoreError \/ JoinRequest = 
      DB.withSession { implicit session =>
        val maybeId = join_table returning join_table.map(_.jid) += join
        maybeId.fold[StoreError \/ JoinRequest](
          -\/(GenericError(s"Error al insertar JoinRequest $join")))(
          _ => \/-(join.copy(jid = maybeId)))
      }
  
    def putMember(member: Member): StoreError \/ Member = 
      DB.withSession { implicit session =>
        val maybeId = member_table returning member_table.map(_.mid) += member
        maybeId.fold[StoreError \/ Member](
          -\/(GenericError(s"Error al insertar Member $member")))(
          _ => \/-(member.copy(mid = maybeId)))
      }
  
    def isMember(uid: Int, gid: Int): Boolean = 
      DB.withSession { implicit session =>
        true
      }

    def isPending(uid: Int, gid: Int): Boolean = 
      DB.withSession { implicit session =>
        true
      }
  }

  trait Services{ self: Store => 
    
    def join(request: JoinRequest): StoreError \/ JoinResponse = {
      val JoinRequest(_, uid, gid) = request  
      for {
        user <- getUser(uid)
        group <- getGroup(gid)
        result <- 
          if (group.must_approve) 
            (putJoin(request) map Left.apply)
          else
            (putMember(Member(None, uid, gid)) map Right.apply)
      } yield result
    }

  }

}
 