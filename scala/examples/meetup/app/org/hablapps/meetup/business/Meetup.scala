package org.hablapps.meetup

package object logic{
  import db.Store
  import domain._

  def join(request: JoinRequest): Store[Either[JoinRequest, Member]] = {
    val JoinRequest(_, uid, gid) = request
    for{
      user <- Store.getUser(uid)
      group <- Store.getGroup(gid)
      memberOrRequest <- 
        Store.If(!group.must_approve)(
          _then = Store.putMember(Member(None, uid, gid)), 
          _else = Store.putJoin(request) unless Store.isPending(uid, gid)
        ) unless Store.isMember(uid, gid)
    } yield memberOrRequest
  }

}