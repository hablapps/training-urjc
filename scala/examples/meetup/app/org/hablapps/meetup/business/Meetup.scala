package org.hablapps.meetup

package object logic{
  import db.Store
  import domain._

  def join(request: JoinRequest): Store[Either[JoinRequest, Member]] = {
    val JoinRequest(_, uid, gid) = request
    for{
      user <- Store.getUser(uid)
      group <- Store.getGroup(gid)
      memberOrRequest <- Store.cond(
        !group.must_approve,
        Store.putMember(Member(None, uid, gid)), 
        Store.putJoin(request) 
          unless Store.isPending(uid, gid)
      ) unless Store.isMember(uid, gid)
    } yield memberOrRequest
  }

}