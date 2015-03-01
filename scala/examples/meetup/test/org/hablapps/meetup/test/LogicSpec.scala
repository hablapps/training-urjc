package org.hablapps.meetup.test

import org.specs2.mutable._

import org.hablapps.meetup.{domain, db, logic}, 
  domain._, logic._, db._

class LogicSpec extends Specification {

  "unirse a un grupo" should {

    "devolver un error si el usuario no existe" in {
      val store1 = MapInterpreter.MapStore()
  
      MapInterpreter.output(store1)(join(JoinRequest(None, 1, 2))) must_== Left(NonExistentEntity(1))
    }

    "devolver un error si el grupo no existe" in {
      val store1 = MapInterpreter.MapStore(
        User(Some(1), "user 1")
      )

      MapInterpreter.output(store1)(join(JoinRequest(None, 1, 2))) must_== Left(NonExistentEntity(2))
    }


  }

  "unirse a un grupo sin restricciones de entrada" should {
    
    "realizarse inmediatamente si el usuario no pertenece ya al mismo" in {

      val store1 = MapInterpreter.MapStore(
        User(Some(1), "user 1"),
        Group(Some(2), "group 1", "CR", false)
      )

      MapInterpreter.output(store1)(join(JoinRequest(None, 1, 2))) must_== Right(Right(Member(Some(3),1,2)))
    }

    "prohibirse si el usuario pertenece ya" in {

      val store1 = MapInterpreter.MapStore(
        User(Some(1), "user 1"),
        Group(Some(2), "group 1", "CR", false)
      )

      val (store2, _) = MapInterpreter.run(join(JoinRequest(None, 1, 2)))(store1)
      MapInterpreter.output(store2)(join(JoinRequest(None, 1, 2))) must beLike{
        case Left(ConstraintFailed(IsMember(1, 2, _))) => ok
        case _ => ko
      }

      /* 
      (for { _ <- run(join(2, 1)); _ <- run(join(2,1)) } yield () ) must_== ...
      */
    }

  }

  "unirse a un grupo con restricciones de entrada" should {
  
    "dejarse pendiente si el usuario no pertenece ya y no estaba pendiente ninguna otra solicitud" in {

      val store1 = MapInterpreter.MapStore(
        User(Some(1), "user 1"),
        Group(Some(2), "group 1", "CR", true)
      )

      MapInterpreter.output(store1)(join(JoinRequest(None, 1, 2))) must_== Right(Left(JoinRequest(Some(3),1,2)))
    }

    "prohibirse si el usuario pertenece ya" in {

      val store1 = MapInterpreter.MapStore(
        User(Some(1), "user 1"),
        Group(Some(2), "group 1", "CR", true),
        Member(Some(3), 1, 2)
      )

      MapInterpreter.output(store1)(join(JoinRequest(None, 1, 2))) must beLike{
        case Left(ConstraintFailed(IsMember(1, 2, _))) => ok
        case _ => ko
      }
    }

    "prohibirse si el usuario no pertenece ya, pero tiene una solicitud pendiente" in {

      val store1 = MapInterpreter.MapStore(
        User(Some(1), "user 1"),
        Group(Some(2), "group 1", "CR", true),
        JoinRequest(Some(3), 1, 2)
      )

      MapInterpreter.output(store1)(join(JoinRequest(None, 1, 2))) must beLike{
        case Left(ConstraintFailed(IsPending(1, 2, _))) => ok
        case _ => ko
      }
    }

  }

}
