package stellar.protocol.result

import cats.data.State
import stellar.protocol.xdr.Decoder
import stellar.protocol.xdr.Encode.int

sealed abstract class BumpSequenceResult(val opResultCode: Int) extends OpResult {
  override val opCode: Int = 11
  override def encode: LazyList[Byte] = int(opResultCode)
}

object BumpSequenceResult extends Decoder[BumpSequenceResult] {
  val decodeOld: State[Seq[Byte], BumpSequenceResult] = int.map {
    case 0 => BumpSequenceSuccess
    case -1 => BumpSequenceBadSeqNo
  }
}

/**
 * BumpSequence operation was successful.
 */
case object BumpSequenceSuccess extends BumpSequenceResult(0)

/**
 * BumpSequence operation failed because the desired sequence number was less than zero.
 */
case object BumpSequenceBadSeqNo extends BumpSequenceResult(-1)
