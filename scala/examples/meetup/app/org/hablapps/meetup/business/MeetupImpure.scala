package org.hablapps.meetup.logic

import play.api.Play.current
import play.api.db.slick.DB

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import scala.slick.driver.MySQLDriver.simple._
import org.hablapps.meetup.domain._
import org.hablapps.meetup.db._, MySQLInterpreter._

object Impure {

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

}
 