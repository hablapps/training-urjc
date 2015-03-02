package org.hablapps.meetup

import scalaz.\/

package object logic{
  import db.Store
  import db.StoreOp
  import domain._
  import Store._

  def join(request: JoinRequest): Store[JoinRequest \/ Member] = {
    val JoinRequest(_, uid, gid) = request
    for{
      user <- Store.getUser(uid)
      group <- Store.getGroup(gid)
      memberOrRequest <- Store.cond(
        !group.must_approve,
        Store.putMember(Member(None, uid, gid)), 
        Store.putJoin(request) 
          unless StoreOp.IsPending(uid, gid)
      ) unless StoreOp.IsMember(uid, gid)
    } yield memberOrRequest
  }

}