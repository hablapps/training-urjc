package org.hablapps.meetup.domain


case class User(
  uid: Option[Int],
  name: String
)

case class Group(
  id: Option[Int], 
  name: String, 
  city: String,
  must_approve: Boolean
)

case class Member(
  mid: Option[Int],
  uid: Int,
  gid: Int
)

case class JoinRequest(
  jid: Option[Int],
  uid: Int,
  gid: Int
)

