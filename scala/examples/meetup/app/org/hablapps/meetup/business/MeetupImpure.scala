package org.hablapps.meetup.logic

import play.api.Play.current
import play.api.db.slick.DB

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import scala.slick.driver.MySQLDriver.simple._
import org.hablapps.meetup.domain._
import org.hablapps.meetup.db.MySQLInterpreter._

object Impure {

  // VERSION 1: NO SEPARATION OF CONCERNS

  def join(request: JoinRequest): JoinResponse = {
    val JoinRequest(_, uid, gid) = request
    DB.withSession { implicit session =>
      val group: Option[Group] = (for { 
        group <- group_table if group.gid === gid
      } yield group).firstOption
     
      val user: Option[User] = (for {
        user <- user_table if user.uid === uid
      } yield user).firstOption
      
      if (!group.isDefined) 
        throw new RuntimeException(s"Group $gid not found")
      else if (!user.isDefined)
        throw new RuntimeException(s"User $uid not found")
      else if (group.get.must_approve) {
        val maybeId = join_table returning join_table.map(_.jid) += request
        Left(request.copy(jid = maybeId))
      } else {
        val mid = member_table returning member_table.map(_.mid) += Member(None, uid, gid)
        Right(Member(mid, uid, gid))
      }
    }
  }

  // VERSION 2: SEPARATION OF CONCERNS

  trait Store{
    def getGroup(gid: Int): Group
    def getUser(uid: Int): User
    def putJoin(join: JoinRequest): JoinRequest
    def putMember(member: Member): Member
    def isMember(uid: Int, gid: Int): Boolean
    def isPending(uid: Int, gid: Int): Boolean
  }

  object MySQLStore extends Store{

    def getGroup(gid: Int): Group = 
      DB.withSession { implicit session =>
        val maybeGroup = (for { 
          group <- group_table if group.gid === gid
        } yield group).firstOption
        maybeGroup.get
      }
     
    def getUser(uid: Int): User =  
      DB.withSession { implicit session =>
        val maybeUser = (for {
          user <- user_table if user.uid === uid
        } yield user).firstOption
        maybeUser.get
      }
      
    def putJoin(join: JoinRequest): JoinRequest = 
      DB.withSession { implicit session =>
        val maybeId = join_table returning join_table.map(_.jid) += join
        join.copy(jid = maybeId)
      }
  
    def putMember(member: Member): Member = 
      DB.withSession { implicit session =>
        val maybeId = member_table returning member_table.map(_.mid) += member
        member.copy(mid = maybeId)
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
    // EL CUADRO PARA REGALAR ... PERO QUE SE ROMPERÁ CON QUE LO TOQUES, O LO CAMBIES DE SITIO
    
    def join(request: JoinRequest): JoinResponse = {
      val JoinRequest(_, uid, gid) = request
      
      val _ = getUser(uid)
      val group = getGroup(gid)
      if (group.must_approve) 
        Left(putJoin(request))
      else
        Right(putMember(Member(None, uid, gid)))
    }

  }

}
 