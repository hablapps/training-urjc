package factorial

import akka.actor.{ ActorSystem, Actor, Props, ActorRef }
import akka.testkit.{ TestActors, TestKit, ImplicitSender, TestActor, TestActorRef, TestProbe }
import org.scalatest.{ WordSpecLike, Matchers, BeforeAndAfterAll }
 
class FactorialActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("Factorial-Actor-Spec"))
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
 
  "A factorial actor" must {

    "return the factorial of a number (without Probe)" in {

      val factorialActorRef = system.actorOf(Props(classOf[FactorialActor]))
      factorialActorRef ! Calculate(5)

      expectMsg(Result(120))

    }

    "return the factorial of a number (with Probe)" in {

      val testFactorialActorRef = TestActorRef[FactorialActor]
      val testFactorialActor = testFactorialActorRef.underlyingActor
 
      val probeChild = new TestProbe(system)
      probeChild.setAutoPilot(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case msg @ Calculate(_) =>
              sender ! Result(24)
              TestActor.NoAutoPilot
          }
      })

      testFactorialActor.child should be(None)
      testFactorialActor.child = Some(probeChild.ref)
      testFactorialActor.child should be(Some(probeChild.ref))

      testFactorialActorRef ! Calculate(5)

      probeChild.expectMsg(Calculate(4))

      expectMsg(Result(120))
    }
  }
}
