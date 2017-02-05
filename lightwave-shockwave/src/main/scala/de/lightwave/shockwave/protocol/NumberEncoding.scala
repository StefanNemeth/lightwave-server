package de.lightwave.shockwave.protocol

import akka.util.ByteString

/**
  * Helper methods for encoding/decoding numbers used by the
  * shockwave protocol
  */
object NumberEncoding {
  /**
    * Encodes short values with a specific length. Usually, outgoing short values
    * only describe packet ids
    */
  def encodeShort(value: Short, length: Int = 2): ByteString = {
    if (value < 0 || length < 1) {
      throw new IllegalArgumentException("Negative short values cannot be encoded properly.")
    }

    ByteString(Seq.fill(length){""}.zipWithIndex.map {
      case (_, index) => (64 + (value >> (6 * (length - (index + 1))) & 0x3f)).toChar
    }.mkString)
  }

  /**
    * Decodes short values out of the whole given byte string.
    * Usually, incoming short values describe packet ids, string lengths and packet lengths
    */
  def decodeShort(encoded: ByteString): Short = {
    val len = encoded.length
    encoded.zipWithIndex.map {
      case (byte, index) => (byte.toInt - 64) * Math.pow(64, len - (index + 1)).toInt
    }.sum.toShort
  }

  /**
    * Encodes int values such as player ids etc. with a dynamic length.
    */
  def encodeInt(signedValue: Int): ByteString = {
    val unsignedValue = Math.abs(signedValue)
    val body = Array.ofDim[Byte](5).zipWithIndex.map {
      case (_, index) if (unsignedValue >> (2 + (6 * index))) > 0 => (64 + (unsignedValue >> (2 + (6 * index)) & 63)).toByte
      case _ => 0.toByte
    }.takeWhile(_ != 0.toByte)

    ByteString.fromArray(Array(((64 + (unsignedValue & 3)) | (body.length + 1) << 3 | (if(signedValue >= 0) 0 else 4)).toByte) ++ body)
  }

  /**
    * Decodes int values out of the given byte string.
    * @return Decoded value and the length of the encoded version
    *         (decodedValue: Int, byteLength: Int)
    */
  def decodeInt(encoded: ByteString): (Int, Int) = {
    val len = encoded(0) >> 3 & 7

    if (len < 1 || len > encoded.length) {
      throw new IllegalArgumentException("Invalid format or insufficient data.")
    }

    (encoded.slice(0, len).zipWithIndex.map {
      case (byte, 0) => byte & 3
      case (byte, index) => (byte & 0x3f) << (2 + 6 * (index - 1))
    }.reduceLeft(_ | _) * (if ((encoded(0) & 4) == 4) -1 else 1), len)
  }
}
