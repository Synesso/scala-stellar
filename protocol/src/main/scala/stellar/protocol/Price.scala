package stellar.protocol

import cats.data.State
import stellar.protocol.xdr.Encode.int
import stellar.protocol.xdr.{Decoder, Encodable, Encode}

case class Price(n: Int, d: Int) extends Encodable {
  def encode: LazyList[Byte] = int(n) ++ int(d)
}

object Price extends Decoder[Price] {
  val decodeOld: State[Seq[Byte], Price] = for {
    n <- int
    d <- int
  } yield Price(n, d)
}
