package org.hablapps.meetup.logic

import play.api.Play.current
import play.api.db.slick.DB

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import scala.slick.driver.MySQLDriver.simple._
import org.hablapps.meetup.domain._
import org.hablapps.meetup.db._, MySQLInterpreter._

object ImpureOO {

  trait Store{
    def getGroup(gid: Int): Group
    def getUser(uid: Int): User
    def putJoin(join: JoinRequest): JoinRequest
    def putMember(member: Member): Member
    def isMember(uid: Int, gid: Int): Boolean
    def isPending(uid: Int, gid: Int): Boolean
  }

  trait MySQLStore extends Store{

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

  object StoreOO extends Services with MySQLStore

}
 