package es.urjc.etsii.dictionary.models

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.cache.Cache
import play.api.Play.current

trait RepoInterpreter {

  type State

  def getState: State

  def setState(next: State): Unit

  def interpreter[A](repo: Repo[A], state: State): Future[(A, State)]
}

trait MapRepoInterpreter extends RepoInterpreter {

  case class State(
    users: Map[String, User] = Map(),
    words: Map[String, String] = Map())

  var state: State = State()

  def getState: State = state

  def setState(next: State) = state = next

  def interpreter[A](repo: Repo[A], state: State): Future[(A, State)] =
    repo match {
      case GetEntry(word, next) => {
        interpreter(next(state.words.get(word)), state)
      }
      case SetEntry(entry, next) => {
        interpreter(next, state.copy(words = state.words + entry))
      }
      case RemoveEntry(word, next) => {
        interpreter(next, state.copy(words = state.words - word))
      }
      case GetUser(nick, next) => {
        interpreter(next(state.users.get(nick)), state)
      }
      case SetUser(user, next) => {
        val nuser = user.nick -> user
        interpreter(next, state.copy(users = state.users + nuser))
      }
      case RemoveUser(nick, next) => {
        interpreter(next, state.copy(users = state.users - nick))
      }
      case CanRead(user, next) => {
        interpreter(next(user.permission.fold(false)(_ => true)), state)
      }
      case CanWrite(user, next) => {
        interpreter(next(user.permission.fold(false)(_ == ReadWrite)), state)
      }
      case Return(value) => Future((value, state))
    }
}

trait CacheRepoInterpreter extends RepoInterpreter {

  type State = Cache.type

  val state = Cache

  def getState = state

  def setState(next: State) = ()

  def interpreter[A](repo: Repo[A], state: State): Future[(A, State)] =
    repo match {
      case GetEntry(word, next) => {
        val definition = state.getAs[String](s"${WORD_NAMESPACE}.$word")
        interpreter(next(definition), state)
      }
      case SetEntry((word, definition), next) => {
        state.set(s"${WORD_NAMESPACE}.$word", definition)
        interpreter(next, state)
      }
      case RemoveEntry(word, next) => {
        state.remove("${WORD_NAMESPACE}.$word")
        interpreter(next, state)
      }
      case GetUser(nick, next) => {
        println(s"GETTING USER ${USER_NAMESPACE}.$nick")
        val user = state.getAs[User](s"${USER_NAMESPACE}.$nick")
        println(user)
        interpreter(next(user), state)
      }
      case SetUser(user, next) => {
        println(s"SETTING USER ${USER_NAMESPACE}.${user.nick}")
        state.set(s"${USER_NAMESPACE}.${user.nick}", user)
        interpreter(next, state)
      }
      case RemoveUser(nick, next) => {
        state.remove("${USER_NAMESPACE}.$nick")
        interpreter(next, state)
      }
      case CanRead(user, next) => {
        interpreter(next(user.permission.fold(false)(_ => true)), state)
      }
      case CanWrite(user, next) => {
        interpreter(next(user.permission.fold(false)(_ == ReadWrite)), state)
      }
      case Return(value) => Future((value, state))
    }

  private val WORD_NAMESPACE = "es.urjc.etsii.dictionary.models.word"
  private val USER_NAMESPACE = "es.urjc.etsii.dictionary.models.user"
}
