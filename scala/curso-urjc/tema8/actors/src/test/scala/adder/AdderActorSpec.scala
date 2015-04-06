package adder

import akka.actor.{ ActorSystem, Actor, Props }
import akka.testkit.{ TestActors, TestKit, ImplicitSender, TestActorRef }
import org.scalatest.{ WordSpecLike, Matchers, BeforeAndAfterAll }
 
class AdderActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("Adder-Actor-Spec"))
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
 
  "An adder actor" must {
 
    "accumulate the values received" in {
 
      val testActorRef = TestActorRef[AdderActor]
      val testActor = testActorRef.underlyingActor

      testActor.accum should be(0)

      testActorRef ! Add(1)

      testActor.accum should be(1)

      testActorRef ! Add(5)

      testActor.accum should be(6)

      testActorRef ! Add(-1)

      testActor.accum should be(5)
    }
  }
}
