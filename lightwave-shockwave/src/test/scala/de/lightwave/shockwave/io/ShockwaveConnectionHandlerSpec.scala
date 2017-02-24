package de.lightwave.shockwave.io

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.io.Tcp.Write
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import de.lightwave.io.tcp.ConnectionHandler.{MessageRead, ReadMessage}
import de.lightwave.io.tcp.protocol.MessageHeader
import de.lightwave.players.model.Player
import de.lightwave.shockwave.handler.MessageHandler.HandleMessage
import de.lightwave.shockwave.io.ShockwaveConnectionHandler.{GetPlayerInformation, SetPlayerInformation}
import de.lightwave.shockwave.io.protocol.OperationCode
import de.lightwave.shockwave.io.protocol.messages.{HelloMessageComposer, PongMessage}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class ShockwaveConnectionHandlerSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll {

  test("Read message in a single chunk") {
    withActor() { (handler, _, _) =>
      handler ! ReadMessage(ByteString("@@B@A"))
      expectMsg(MessageRead(MessageHeader(0, 1), ByteString("")))
    }
  }

  test("Read message in multiple chunks") {
    withActor() { (handler, _, _) =>
      handler ! ReadMessage(ByteString("@@E@B"))
      expectNoMsg()

      handler ! ReadMessage(ByteString("AB"))
      expectNoMsg()

      handler ! ReadMessage(ByteString("C"))
      expectMsg(MessageRead(MessageHeader(3, 2), ByteString("ABC")))
    }
  }

  test("Receive initial hello message") {
    withActor() { (handler, connection, _) =>
      connection.expectMsg(Write(HelloMessageComposer.compose()))
    }
  }

  test("Forward messages to message handler") {
    withActor() { (handler, _, messageHandler) =>
      handler ! MessageRead(MessageHeader(0, OperationCode.Incoming.Pong), ByteString.empty)
      messageHandler.expectMsg(HandleMessage(PongMessage))
    }
  }

  test("Set player information") {
    withActor() { (handler, _, _) =>
      val player = Player(None, "")

      handler ! SetPlayerInformation(player)
      assert(handler.underlyingActor.playerInformation === Some(player))
    }
  }

  test("Get player information") {
    withActor() { (handler, _, _) =>
      val player = Player(None, "")

      handler ! GetPlayerInformation
      expectMsg(None)

      handler ! SetPlayerInformation(player)
      handler ! GetPlayerInformation
      expectMsg(Some(player))
    }
  }

  private def withActor()(testCode: (TestActorRef[ShockwaveConnectionHandler], TestProbe, TestProbe) => Any) = {
    val connectionProbe = TestProbe()
    val messageHandlerProbe = TestProbe()

    testCode(TestActorRef[ShockwaveConnectionHandler](
      ShockwaveConnectionHandler.props(messageHandlerProbe.ref)(new InetSocketAddress("127.0.0.1", 8123), connectionProbe.ref)
    ), connectionProbe, messageHandlerProbe)
  }

  override def afterAll: Unit = {
    shutdown()
  }
}
