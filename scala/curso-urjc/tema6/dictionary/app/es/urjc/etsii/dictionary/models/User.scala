package es.urjc.etsii.dictionary.models

case class User(
    name: String, 
    last: String, 
    permission: Option[Permission] = None) {
  def nick: String = s"${name.toLowerCase}${last.toLowerCase}"
}
