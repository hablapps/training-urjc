package es.urjc.etsii.dictionary.services

import es.urjc.etsii.dictionary.models._

trait WordServices {

  val getEntry: String => Repo[Option[String]] = GetEntry(_, Return(_))

  val setEntry: ((String, String)) => Repo[Unit] = SetEntry(_, Return(()))

  val removeEntry: String => Repo[Unit] = RemoveEntry(_, Return(()))
}
