package org.hablapps.meetup

package object logic{
  import db._, Store._
  import domain._

  def join(request: JoinRequest): Store[Either[JoinRequest, Member]] = {
    val JoinRequest(_, uid, gid) = request
    for{
      user <- Store.getUser(uid)
      group <- Store.getGroup(gid)
      joinOrMember <- 
        Store.If(!group.must_approve)(
          _then = Store.putMember(Member(None, uid, gid)), 
          _else = Store.putJoin(request) unless Store.isPending(uid, gid)
        ) unless Store.isMember(uid, gid)
    } yield joinOrMember
  }

  def joinWithMonadAndUnlessAndIf(request: JoinRequest): Store[Either[JoinRequest, Member]] = {
    val JoinRequest(_, uid, gid) = request
    Store.getUser(uid) flatMap { user =>
      Store.getGroup(gid) flatMap { 
        group => (
          Store.If(!group.must_approve)(
            _then = Store.putMember(Member(None, uid, gid)),
            _else = Store.putJoin(request) unless Store.isPending(uid, gid)
          )
        ) unless Store.isMember(uid, gid)
      }
    }
  }

  def joinWithMonadAndUnless(request: JoinRequest): Store[Either[JoinRequest, Member]] = {
    val JoinRequest(_, uid, gid) = request
    Store.getUser(uid) flatMap { user =>
      Store.getGroup(gid) flatMap { 
        group => (
          if (!group.must_approve)
            Store.putMember(Member(None, uid, gid)) map { member => 
              Right(member)
            }
          else 
            Store.putJoin(request) map { join => 
              Left(join)
            } unless Store.isPending(uid, gid)
        ) unless Store.isMember(uid, gid)
      }
    }
  }

  def joinWithMonad(request: JoinRequest): Store[Either[JoinRequest, Member]] = {
    val JoinRequest(_, uid, gid) = request
    Store.getUser(uid) flatMap { user =>
      Store.getGroup(gid) flatMap { group =>
        Store.isMember(uid, gid) flatMap { isMember => 
          if (isMember)
            Fail(GenericError(".."))
          else 
            if (!group.must_approve)
              Store.putMember(Member(None, uid, gid)) map { member => 
                Right(member)
              }
            else 
              Store.isPending(uid, gid) flatMap { isPending => 
                if (isPending)
                  Fail(GenericError(".."))
                else 
                  Store.putJoin(request) map { join => 
                    Left(join)
                  }
              }
        }
      }
    }
  }

  def joinPlain(request: JoinRequest): Store[Either[JoinRequest, Member]] = {
    val JoinRequest(_, uid, gid) = request
    GetUser(uid, user =>
      GetGroup(gid, group =>
        IsMember(uid, gid, isMember => 
          if (isMember)
            Fail(GenericError(".."))
          else 
            if (!group.must_approve)
              PutMember(Member(None, uid, gid), member => 
                Return(Right(member))
              )
            else 
              IsPending(uid, gid, isPending => 
                if (isPending)
                  Fail(GenericError(".."))
                else 
                  PutJoin(request, join => 
                    Return(Left(join))
                  )
              )
        )
      )
    )
  }

  def joinWithForAndUnless(request: JoinRequest): Store[Either[JoinRequest, Member]] = {
    val JoinRequest(_, uid, gid) = request
    for {
      user <- Store.getUser(uid) 
      group <- Store.getGroup(gid)
      joinOrMember <- (
        if (!group.must_approve)
          for {
            member <- Store.putMember(Member(None, uid, gid))
          } yield Right(member)
        else 
          for {
            join <- Store.putJoin(request) unless Store.isPending(uid, gid)
          } yield Left(join)
      ) unless Store.isMember(uid, gid)
    } yield joinOrMember
  }

  def joinWithFor(request: JoinRequest): Store[Either[JoinRequest, Member]] = {
    val JoinRequest(_, uid, gid) = request
    for {
      user <- Store.getUser(uid) 
      group <- Store.getGroup(gid)
      isMember <- Store.isMember(uid, gid)
      joinOrMember <- 
        if (isMember)
          Fail(GenericError(".."))
        else if (!group.must_approve)
          for {
            member <- Store.putMember(Member(None, uid, gid))
          } yield Right(member)
        else 
          for {
            isPending <- Store.isPending(uid, gid)
            join <- 
              if (isPending)
                Fail(GenericError(".."))
              else 
                for {
                  join <- Store.putJoin(request)
                } yield Left(join)
          } yield join 
    } yield joinOrMember
  }


}