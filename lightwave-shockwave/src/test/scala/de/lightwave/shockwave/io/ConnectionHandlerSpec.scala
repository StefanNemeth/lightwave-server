package de.lightwave.shockwave.io

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.io.Tcp.Write
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import de.lightwave.shockwave.io.ConnectionHandler.{MessageRead, ReadMessage}
import de.lightwave.shockwave.io.protocol.message.MessageHeader
import de.lightwave.shockwave.io.protocol.message.incoming.miscellaneous.PongMessage
import de.lightwave.shockwave.io.protocol.message.outgoing.miscellaneous.PingMessage
import org.scalatest.{BeforeAndAfterAll, FunSuite, FunSuiteLike}

class ConnectionHandlerSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
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

  test("Receive initial ping") {
    withActor() { (handler, connection, _) =>
      connection.expectMsg(Write(PingMessage.compose()))
    }
  }

  test("Forward messages to message handler") {
    withActor() { (handler, _, messageHandler) =>
      handler ! MessageRead(MessageHeader(0, 1), ByteString.empty)
      messageHandler.expectMsg(PongMessage())
    }
  }

  private def withActor()(testCode: (TestActorRef[ConnectionHandler], TestProbe, TestProbe) => Any) = {
    val connectionProbe = TestProbe()
    val messageHandlerProbe = TestProbe()

    testCode(TestActorRef[ConnectionHandler](
      ConnectionHandler.props(new InetSocketAddress("127.0.0.1", 8123), connectionProbe.ref, messageHandlerProbe.ref)
    ), connectionProbe, messageHandlerProbe)
  }

  override def afterAll: Unit = {
    shutdown()
  }
}
