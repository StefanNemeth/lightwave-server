package de.lightwave.services.pubsub

import akka.actor.ActorSystem
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class BroadcasterSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll {

  test("Subscribe to topic") {
    withActor("topic1") { (broadcaster, mediator) =>
      val testRef = TestProbe().ref

      broadcaster ! Broadcaster.Subscribe(testRef)
      mediator.expectMsg(Subscribe("topic1", testRef))
    }
  }

  test("Unsubscribe from topic") {
    withActor("topic1") { (broadcaster, mediator) =>
      val testRef = TestProbe().ref

      broadcaster ! Broadcaster.Unsubscribe(testRef)
      mediator.expectMsg(Unsubscribe("topic1", testRef))
    }
  }

  test("Publish to topic") {
    withActor("topic1") { (broadcaster, mediator) =>
      broadcaster ! Broadcaster.Publish("hello")
      mediator.expectMsg(Publish("topic1", "hello"))
    }
  }

  private def withActor(topic: String)(testCode: (TestActorRef[Broadcaster], TestProbe) => Any): Unit = {
    val mediator = TestProbe()

    testCode(TestActorRef[Broadcaster](Broadcaster.props(topic)(mediator.ref)), mediator)
  }
}
