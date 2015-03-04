package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current
import play.api.db.slick.DB

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import play.api.libs.concurrent.Akka

import org.hablapps.meetup.{domain, db, logic}, 
  logic._,
  db._,
  domain._

object MembersAsync extends Controller{

  import Members.{fromHTTP, toHTTP => toHTTP_plain}

  def add(gid: Int): Action[Int] =
    Action.async(parse.json[Int]) { 
      fromHTTP(gid) andThen 
      join          andThen
      interpreter   andThen
      toHTTP
    }
  
  lazy val blocking_ec: ExecutionContext = Akka.system.dispatchers.lookup("play.akka.actor.blocking-dispatcher")
  implicit val default_ec: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext
  
  def interpreter[U]: Store[U] => Future[Either[StoreError, U]] = 
    MySQLInterpreter.runAsync[U](_)(blocking_ec)
  
  def toHTTP(response: Future[Either[StoreError, Either[JoinRequest, Member]]]): Future[Result] =
    response.map(toHTTP_plain)(default_ec)

}