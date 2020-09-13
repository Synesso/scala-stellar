package stellar.protocol

import cats.data.NonEmptyList
import okio.ByteString
import stellar.protocol.op.Operation
import stellar.protocol.xdr.Encodable
import stellar.protocol.xdr.Encode.{arr, int, long}

/**
 * TODO
 */
case class Transaction(
  network: NetworkId,
  source: AccountId,
  sequence: Long,
  operations: NonEmptyList[Operation],
  maxFee: Int,
  keys: List[SigningKey] // TODO - this typing will hurt when decoding.
) extends Encodable {

  def encodeBase: LazyList[Byte] =
    int(2) ++
      source.encode ++
      int(maxFee) ++
      long(sequence) ++
      int(0) ++ // TODO - timebounds
      int(0) ++ // TODO - memo
      arr(operations.toList) ++
      int(0)

  override def encode: LazyList[Byte] = {
    val encoded = encodeBase
    if (keys.isEmpty) encoded
    else {
      val hash = new ByteString((network.hash ++ encoded).toArray).sha256()
      val signatures = keys.map(_.sign(hash))
      encoded ++ arr(signatures)
    }
  }
}
