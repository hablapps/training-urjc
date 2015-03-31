package es.urjc.etsii.dictionary.services

import es.urjc.etsii.dictionary.models._
import Repo._

trait PermissionServices { this: UserServices with WordServices =>

  val canRead: User => Repo[Boolean] = CanRead(_, Return(_))

  val canWrite: User => Repo[Boolean] = CanWrite(_, Return(_))

  val nickCanRead: String => Repo[Option[Boolean]] =
    optComposeK(canRead, getUser)

  val nickCanWrite: String => Repo[Option[Boolean]] =
    optComposeK(canWrite, getUser)

  val authorizedSearch: Tuple2[String, String] => Repo[Option[String]] =
    if_K(
      cond = nickCanRead,
      then_K = getEntry,
      else_K = _ => Return(None))

  val authorizedAdd: Tuple2[String, Tuple2[String, String]] => Repo[Option[Unit]] =
    if_K(
      cond = nickCanWrite,
      then_K = setEntry,
      else_K = _ => Return(None))
}

