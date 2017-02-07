package de.lightwave.shockwave.io.protocol

import akka.util.ByteString
import org.scalatest.FunSuite

class NumberEncodingSpec extends FunSuite {
  test("Encode short to byte string") {
    assert(NumberEncoding.encodeShort(1.toShort).utf8String == "@A")
  }

  test("Do not encode integer of negative value or without length") {
    intercept[IllegalArgumentException] {
      NumberEncoding.encodeShort(-1)
    }

    intercept[IllegalArgumentException] {
      NumberEncoding.encodeShort(1, 0)
    }
  }

  test("Encode integer to byte string") {
    assert(NumberEncoding.encodeInt(1).utf8String == "I")
  }

  test("Decode short out of byte string") {
    assert(NumberEncoding.decodeShort(ByteString("@B")) == 2)
  }

  test("Decode integer out of byte string") {
    assert(NumberEncoding.decodeInt(ByteString("J"))._1 == 2)
  }

  test("Do not decode integer out of a byte string using an invalid format") {
    intercept[IllegalArgumentException] {
      NumberEncoding.decodeInt(ByteString("A"))
    }
  }
}
