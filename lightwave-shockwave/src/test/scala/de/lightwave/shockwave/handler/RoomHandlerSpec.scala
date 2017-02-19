package de.lightwave.shockwave.handler

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.lightwave.shockwave.io.protocol.messages.GetHeightmapMessage
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class RoomHandlerSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll {

  test("Get heightmap") {
    withActor() { (handler, engine) =>
      handler ! GetHeightmapMessage
    }
  }

  private def withActor()(testCode: (ActorRef, TestProbe) => Any) = {
    val roomEngine = TestProbe()

    testCode(system.actorOf(RoomHandler.props(roomEngine.ref)), roomEngine)
  }
}
