package de.lightwave.shockwave.handler

import akka.actor.{ActorRef, ActorSystem}
import akka.io.Tcp.Write
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.lightwave.rooms.engine.RoomEngine.{InitializeRoom, Initialized}
import de.lightwave.shockwave.io.ShockwaveConnectionHandler.EnterRoom
import de.lightwave.shockwave.io.protocol.messages._
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class NavigatorHandlerSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll {

  test("Get test room in recommended room list") {
    withActor() { (handler, _) =>
      handler ! GetRecommendedRoomsMessage
      expectMsg(Write(RecommendedRoomListMessageComposer.compose(Seq(NavigatorHandler.TestRoom))))
    }
  }

  test("Receive flat information of test room") {
    withActor() { (handler, _) =>
      handler ! GetFlatInformationMessage(0) // TODO: id doesn't matter yet (test case) -- see also below
      expectMsg(Write(FlatInformationMessageComposer.compose(NavigatorHandler.TestRoom)))
    }
  }

  test("Get loading advertisement data") {
    withActor() { (handler, _) =>
      handler ! GetLoadingAdvertisementMessage
      expectMsg(Write(LoadingAdvertisementDataMessageComposer.compose()))
    }
  }

  test("Initiate room loading of test room") {
    withActor() { (handler, _) =>
      handler ! RoomDirectoryMessage(0)
      expectMsg(Write(InitiateRoomLoadingMessageComposer.compose))
    }
  }

  test("Get access to open test room") { // will soon check for ban, password, etc.
    withActor() { (handler, _) =>
      handler ! TryFlatMessage(0)
      expectMsg(Write(FlatLetInMessageComposer.compose))
    }
  }

  test("Create room engine, enter room and receive ready message") {
    withActor() { (handler, roomRegion) =>
      handler ! GoToFlatMessage(0)

      roomRegion.expectMsg(InitializeRoom(NavigatorHandler.TestRoom))
      roomRegion.reply(Initialized)

      expectMsg(EnterRoom(roomRegion.ref))
      expectMsg(Write(RoomReadyMessageComposer.compose(
        NavigatorHandler.TestRoom.modelId.getOrElse("model_a"), NavigatorHandler.TestRoom.id.getOrElse(1)
      )))
    }
  }

  private def withActor()(testCode: (ActorRef, TestProbe) => Any) = {
    val roomRegion = TestProbe()

    testCode(system.actorOf(NavigatorHandler.props(roomRegion.ref)), roomRegion)
  }
}
