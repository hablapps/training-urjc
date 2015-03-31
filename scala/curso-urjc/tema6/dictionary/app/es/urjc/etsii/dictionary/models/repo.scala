package es.urjc.etsii.dictionary.models

import scala.language.implicitConversions

sealed trait Repo[A] {

  def map[B](f: A => B) = flatMap[B](a => Return(f(a)))

  def flatMap[B](f: A => Repo[B]): Repo[B] = this match {
    case GetEntry(word, next) => GetEntry(word, next(_) flatMap f)
    case SetEntry(entry, next) => SetEntry(entry, next flatMap f)
    case RemoveEntry(word, next) => RemoveEntry(word, next flatMap f)
    case GetUser(nick, next) => GetUser(nick, next(_) flatMap f)
    case SetUser(user, next) => SetUser(user, next flatMap f)
    case RemoveUser(nick, next) => RemoveUser(nick, next flatMap f)
    case CanRead(user, next) => CanRead(user, next(_) flatMap f)
    case CanWrite(user, next) => CanWrite(user, next(_) flatMap f)
    case Return(value) => f(value)
  }
}

case class Return[A](value: A) extends Repo[A]

/* words */

case class GetEntry[A](word: String, next: Option[String] => Repo[A]) 
    extends Repo[A]

case class SetEntry[A](entry: (String, String), next: Repo[A])
    extends Repo[A]

case class RemoveEntry[A](word: String, next: Repo[A])
    extends Repo[A]

/* users */

case class GetUser[A](nick: String, next: Option[User] => Repo[A]) 
    extends Repo[A]

case class SetUser[A](user: User, next: Repo[A])
    extends Repo[A]

case class RemoveUser[A](nick: String, next: Repo[A])
    extends Repo[A]

/* permission */

case class CanRead[A](user: User, next: Boolean => Repo[A])
    extends Repo[A]

case class CanWrite[A](user: User, next: Boolean => Repo[A])
    extends Repo[A]


object Repo {

  def composeK[A, B, C](
    g: B => Repo[C],
    f: A => Repo[B]): A => Repo[C] = f(_) flatMap g

  def optComposeK[A, B, C](
      g: B => Repo[Option[C]],
      f: A => Repo[Option[B]]): A => Repo[Option[C]] =
    f andThen (_.flatMap(_.map(g(_)).getOrElse(Return(None))))

  implicit def optTransformer[A, B](f: A => Repo[B]): A => Repo[Option[B]] = {
    f andThen (_.map(Option.apply))
  }

  def if_K[A, B, C](
      cond: A => Repo[Option[Boolean]],
      then_K: B => Repo[Option[C]],
      else_K: B => Repo[Option[C]]): Tuple2[A, B] => Repo[Option[C]] = {
    case (a, b) => {
      cond(a).flatMap { ob =>
        ob.map(if (_) then_K(b) else else_K(b)).getOrElse(Return(None))
      }
    }
  }
}
