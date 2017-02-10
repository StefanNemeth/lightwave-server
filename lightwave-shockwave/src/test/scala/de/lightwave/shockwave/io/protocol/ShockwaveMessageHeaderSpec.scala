package de.lightwave.shockwave.io.protocol

import akka.util.ByteString
import de.lightwave.io.tcp.protocol.MessageHeader
import org.scalatest.FunSuite

class ShockwaveMessageHeaderSpec extends FunSuite {
  test("Create message header from byte string") {
    assert(ShockwaveMessageHeader.parse(ByteString("@@C@A")) == MessageHeader(1, 1))
  }

  test("Create message header from byte string with packet length smaller than 2") {
    assert(ShockwaveMessageHeader.parse(ByteString("@@A@A")) == MessageHeader(0, 1))
  }
}
