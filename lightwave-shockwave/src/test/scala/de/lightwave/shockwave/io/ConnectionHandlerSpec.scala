package de.lightwave.shockwave.io

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.io.Tcp.Write
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import de.lightwave.shockwave.io.ConnectionHandler.{ReadMessage, MessageRead}
import de.lightwave.shockwave.io.protocol.message.MessageHeader
import de.lightwave.shockwave.io.protocol.message.outgoing.miscellaneous.PingMessage
import org.scalatest.{BeforeAndAfterAll, FunSuite, FunSuiteLike}

class ConnectionHandlerSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll {

  test("Read message in a single chunk") {
    withActor() { (handler, _) =>
      handler ! ReadMessage(ByteString("@@B@A"))
      expectMsg(MessageRead(MessageHeader(0, 1), ByteString("")))
    }
  }

  test("Read message in multiple chunks") {
    withActor() { (handler, _) =>
      handler ! ReadMessage(ByteString("@@E@B"))
      expectNoMsg()

      handler ! ReadMessage(ByteString("AB"))
      expectNoMsg()

      handler ! ReadMessage(ByteString("C"))
      expectMsg(MessageRead(MessageHeader(3, 2), ByteString("ABC")))
    }
  }

  test ("Receive initial ping") {
    withActor() { (handler, connection) =>
      connection.expectMsg(Write(PingMessage.compose()))
    }
  }

  private def withActor()(testCode: (TestActorRef[ConnectionHandler], TestProbe) => Any) = {
    val probe = TestProbe()

    testCode(TestActorRef[ConnectionHandler](
      ConnectionHandler.props(new InetSocketAddress("127.0.0.1", 8123), probe.ref)
    ), probe)
  }

  override def afterAll: Unit = {
    shutdown()
  }
}
