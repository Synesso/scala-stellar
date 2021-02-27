package stellar.protocol

import cats.data.State
import okio.ByteString
import stellar.protocol.xdr.{Decoder, Encodable, Encode}

case class Signature(data: ByteString, hint: ByteString) extends Encodable {
  def encode: LazyList[Byte] = Encode.bytes(4, hint) ++ Encode.bytes(data)
}

object Signature extends Decoder[Signature] {
  val decodeOld: State[Seq[Byte], Signature] = for {
    hint <- bytes(4).map(_.toArray).map(new ByteString(_))
    data <- bytes.map(_.toArray).map(new ByteString(_))
  } yield Signature(data, hint)
}
