package stellar.protocol

import cats.data.State
import stellar.protocol.xdr.ByteArrays.paddedByteArray
import stellar.protocol.xdr.Encode.{bytes, int}
import stellar.protocol.xdr.{ByteArrays, Decoder, Encodable, Encode}

sealed trait Asset extends Encodable {
  val code: String

  def apply(units: Long): Amount = Amount(this, units)
}

/**
 * The network's native asset, XLM.
 */
case object Lumens extends Asset {
  override val code: String = "XLM"
  override def encode: LazyList[Byte] = int(0)
}

/**
 * An account-defined custom asset.
 */
case class Token(code: String, issuer: AccountId) extends Asset {
  require(code.length >= 1 && code.length <= 12)

  override def encode: LazyList[Byte] =
    if (code.length <= 4) int(1) ++ bytes(4, paddedByteArray(code, 4)) ++ issuer.encode
    else int(2) ++ bytes(12, paddedByteArray(code, 12)) ++ issuer.encode
}

object Asset extends Decoder[Asset] {
  val decode :State[Seq[Byte], Asset] = switch(
    State.pure(Lumens),
    bytes(4)
      .flatMap(code => AccountId.decode
        .map(Token(ByteArrays.paddedByteArrayToString(code.toArray), _))),
    bytes(12)
      .flatMap(code => AccountId.decode
        .map(Token(ByteArrays.paddedByteArrayToString(code.toArray), _)))
  )
}