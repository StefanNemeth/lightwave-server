package de.lightwave.shockwave.handler

import akka.actor.{ActorRef, ActorSystem}
import akka.io.Tcp.Write
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.lightwave.migration.ShockwaveMigration
import de.lightwave.rooms.engine.mapping.MapCoordinator.GetAbsoluteHeightMap
import de.lightwave.shockwave.io.protocol.messages.{GetHeightmapMessage, HeightmapMessageComposer}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class RoomHandlerSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll {

  test("Get heightmap") {
    withActor() { (handler, connection, engine) =>
      val map = IndexedSeq(IndexedSeq(None))
      handler ! GetHeightmapMessage

      engine.expectMsg(GetAbsoluteHeightMap)
      engine.reply(map)

      connection.expectMsg(Write(HeightmapMessageComposer.compose(map)))
    }
  }

  private def withActor()(testCode: (ActorRef, TestProbe, TestProbe) => Any) = {
    val connection = TestProbe()
    val roomEngine = TestProbe()

    testCode(system.actorOf(RoomHandler.props(connection.ref, roomEngine.ref)), connection, roomEngine)
  }
}
