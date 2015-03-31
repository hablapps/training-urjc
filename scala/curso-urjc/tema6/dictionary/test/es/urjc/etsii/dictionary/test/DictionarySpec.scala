package es.urjc.etsii.dictionary.test

import scala.language.postfixOps

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalatest._
import org.scalatest.mock._
import org.scalatestplus.play._

import play.api._
import play.api.cache._
import play.api.libs.json._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import es.urjc.etsii.dictionary.{ models, controllers, services }
import models._
import services._
import controllers._

class DictionarySpec extends PlaySpec with Results with OneAppPerSuite {

  object FakeDictionaryController extends DictionaryController with MapRepoInterpreter

  import FakeDictionaryController._

  def extract(result: Future[(Result, State)]): (Future[Result], State) =
    (result.map(_._1), Await.result(result, 10 seconds)._2)

  def testableSearch: State => Request[AnyContent] => Future[Tuple2[Result, State]] =
    searchBuilder.toTestableAction _

  def testableAdd: State => Request[Tuple2[String, String]] => Future[Tuple2[Result, State]] =
    addBuilder.toTestableAction _

  "add service" should {

    "allow adding new words if the user is empowered to do so" in {
      val request = FakeRequest(
        POST, "/", 
	FakeHeaders(Seq(("user", Seq("mr_proper")))),
    	("new", "a new definition"))
      val old = State(
	users = Map("mr_proper" -> User("Mr", "Proper", Option(ReadWrite))))

      val (result, next) = 
	extract(testableAdd(old)(request))

      status(result) mustEqual CREATED
      next mustEqual State(
	users = Map("mr_proper" -> User("Mr", "Proper", Option(ReadWrite))),
	words = Map("new" -> "a new definition"))
    }

    "fail if the user is not empowered to do so" in {
      val request = FakeRequest(
    	POST, "/", 
    	FakeHeaders(Seq(("user", Seq("don_limpio")))),
    	("new", "a brand new definition"))
      val old = State(
	users = Map("don_limpio" -> User("Don", "Limpio", Option(Read))))
	
      val (result, next) = 
	extract(testableAdd(old)(request))

      status(result) mustEqual FORBIDDEN
      contentAsString(result) mustEqual "Could not add the new word"
      next mustEqual old
    }

    "fail if the user does not provide a `user` request" in {
      val old = State()
      val request = FakeRequest(POST, "/", FakeHeaders(), 
        ("new", "a brand new definition"))

      val (result, next) = 
	extract(testableAdd(old)(request))

      status(result) mustEqual FORBIDDEN
      contentAsString(result) mustEqual "Could not add the new word"
      next mustEqual next
    }
  }

  "search service" should {

    "find an existing word if the user is empowered to do so" in {
      val old = State(
	users = Map("don_limpio" -> User("Don", "Limpio", Option(Read))),
	words = Map("known" -> "a very well known word"))
      val request = 
        FakeRequest(GET, s"/known").withHeaders(("user" -> "don_limpio"))

      val (result, next) =
	extract(testableSearch(old)(request))

      status(result) mustEqual OK
      contentAsString(result) mustEqual "a very well known word"
      next mustEqual old
    }

    "not find a non-existing word" in {
      val word = "unknown"
      val old = State(
	users = Map("don_limpio" -> User("Don", "Limpio", Option(Read))),
	words = Map())
      val request = 
	FakeRequest(GET, s"/$word").withHeaders(("user" -> "don_limpio"))

      val (result, next) =
	extract(testableSearch(old)(request))

      status(result) mustEqual NOT_FOUND
      contentAsString(result) mustEqual s"Could not find the requested word"
      next mustEqual old
    }
  }
}
