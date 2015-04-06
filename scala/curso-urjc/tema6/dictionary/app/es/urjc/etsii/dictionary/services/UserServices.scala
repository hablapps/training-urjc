package es.urjc.etsii.dictionary.services

import es.urjc.etsii.dictionary.models._

trait UserServices {

  val getUser: String => Repo[Option[User]] = GetUser(_, Return(_))

  val setUser: User => Repo[Unit] = SetUser(_, Return(()))

  val removeUser: String => Repo[Unit] = RemoveUser(_, Return(()))
}
