package de.lightwave.shockwave.io.protocol.message

import akka.util.ByteString
import org.scalatest.FunSuite

class MessageHeaderSpec extends FunSuite {
  test("Create message header from byte string") {
    assert(MessageHeader.from(ByteString("@@C@A")) == MessageHeader(1, 1))
  }

  test("Create message header from byte string with packet length smaller than 2") {
    assert(MessageHeader.from(ByteString("@@A@A")) == MessageHeader(0, 1))
  }
}
