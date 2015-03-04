package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current
import play.api.db.slick.DB

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import scala.slick.driver.MySQLDriver.simple._

import org.hablapps.meetup.{domain, db, logic}, 
  logic._,
  db._,
  domain._,
  MySQLInterpreter._

object Monolithic extends Controller{

  def add(gid: Int): Action[Int] = 
    Action(parse.json[Int]) { 
      implicit request => 
        DB.withSession { implicit session =>
          val uid = request.body

          val must_approve = (for { 
            group <- group_table if group.gid === gid
          } yield group.must_approve).firstOption
          
          val exists_user = (for {
            user <- user_table if user.uid === uid
          } yield user).firstOption
          
          if (!must_approve.isDefined) 
            NotFound(s"Group $gid not found")
          else if (!exists_user.isDefined)
            NotFound(s"User $uid not found")
          else must_approve match {
            case Some(true) => 
              Accepted("pending")
            case _ => 
              try {
                implicit val MemberFormat = Json.writes[Member]
                val mid = member_table returning member_table.map(_.mid) += Member(None, uid, gid)
                Created(Json.toJson(Member(mid,uid, gid)))
              } catch {
                case e : MySQLIntegrityConstraintViolationException => 
                  Forbidden("Already a member")
              }
          }
        }
    }
}
