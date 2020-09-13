package stellar.protocol.xdr

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Instant

import com.typesafe.scalalogging.LazyLogging
import okio.ByteString

trait Encodable {
  def encode: LazyList[Byte]
  def encodeDiscriminated: LazyList[Byte] = encode
  def encodeXdr: String = new ByteString(encode.toArray).base64()
}

object Encode extends LazyLogging {

  def int(i: Int): LazyList[Byte] = {
    val buffer = ByteBuffer.allocate(4)
    buffer.putInt(i)
    logger.trace("Encoding {} as {}", i, buffer.array())
    buffer.array().to(LazyList)
  }

  def long(l: Long): LazyList[Byte] = {
    val buffer = ByteBuffer.allocate(8)
    buffer.putLong(l)
    logger.trace("Encoding {} as {}", l, buffer.array())
    buffer.array().to(LazyList)
  }

  def instant(i: Instant): LazyList[Byte] = long(i.getEpochSecond)

  def bytes(len: Int, bs: ByteString): LazyList[Byte] = bytes(len, bs.toByteArray)
  def bytes(len: Int, bs: Array[Byte]): LazyList[Byte] = bytes(len, bs.toList)
  def bytes(len: Int, bs: List[Byte]): LazyList[Byte] = {
    require(bs.length == len)
    logger.trace("Encoding {}", bs)
    bs.to(LazyList)
  }

  def bytes(bs: ByteString): LazyList[Byte] = bytes(bs.toByteArray)
  def bytes(bs: Array[Byte]): LazyList[Byte] = bytes(bs.toList)
  def bytes(bs: List[Byte]): LazyList[Byte] = {
    logger.trace("Encoding with length {}, {}", bs.length, bs)
    int(bs.length) ++ bs
  }

  def padded(bs: Array[Byte]): LazyList[Byte] = padded(bs.toList)
  def padded(bs: List[Byte]): LazyList[Byte] = {
    val multipleOf: Int = 4
    val filler = Array.fill[Byte]((multipleOf - (bs.length % multipleOf)) % multipleOf)(0)
    bytes(bs) ++ filler
  }

  def string(s: String): LazyList[Byte] = padded(s.getBytes(UTF_8))

  def opt(o: Option[Encodable], ifPresent: Int = 1, ifAbsent: Int = 0): LazyList[Byte] =
    o.map(t => int(ifPresent) ++ t.encode).getOrElse(int(ifAbsent))

  private def optT[T](o: Option[T], encode: T => LazyList[Byte]) =
    o.map(encode).map(int(1) ++ _).getOrElse(int(0))

  def optInt(o: Option[Int]): LazyList[Byte] = optT(o, int)

  def optLong(o: Option[Long]): LazyList[Byte] = optT(o, long)

  def optString(o: Option[String]): LazyList[Byte] = optT(o, string)

  def optBytes(o: Option[List[Byte]]): LazyList[Byte] = optT(o, bytes(_: List[Byte]))

  def arr(xs: List[Encodable]): LazyList[Byte] = int(xs.size) ++ xs.flatMap(_.encode)

  def arrString(xs: List[String]): LazyList[Byte] = int(xs.size) ++ xs.flatMap(string)

  def bool(b: Boolean): LazyList[Byte] = if (b) int(1) else int(0)

}

case class Encoded(encode: LazyList[Byte]) extends Encodable