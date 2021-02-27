package stellar.protocol.result

import cats.data.State
import stellar.protocol.xdr.Decoder
import stellar.protocol.xdr.Encode.{int, long}

sealed abstract class AccountMergeResult(opResultCode: Int) extends OpResult {
  override val opCode: Int = 8
  override def encode: LazyList[Byte] = int(opResultCode)
}

object AccountMergeResult extends Decoder[AccountMergeResult] {
  val decodeOld: State[Seq[Byte], AccountMergeResult] = int.flatMap {
    case 0 => long.map(AccountMergeSuccess)
    case -1 => State.pure(AccountMergeMalformed)
    case -2 => State.pure(AccountMergeNoAccount)
    case -3 => State.pure(AccountMergeImmutable)
    case -4 => State.pure(AccountMergeHasSubEntries)
    case -5 => State.pure(AccountMergeSeqNumTooFar)
    case -6 => State.pure(AccountMergeDestinationFull)
  }
}

/**
 * AccountMerge operation was successful.
 */
case class AccountMergeSuccess(sourceBalance: Long) extends AccountMergeResult(0) {
  override def encode: LazyList[Byte] = super.encode ++ long(sourceBalance)
}

/**
 * AccountMerge operation failed because the request was malformed.
 * E.g. The source and destination accounts are the same.
 */
case object AccountMergeMalformed extends AccountMergeResult(-1)

/**
 * AccountMerge operation failed because the destination account does not exist.
 */
case object AccountMergeNoAccount extends AccountMergeResult(-2)

/**
 * AccountMerge operation failed because the source account has the AUTH_IMMUTABLE flag set.
 */
case object AccountMergeImmutable extends AccountMergeResult(-3)

/**
 * AccountMerge operation failed because the source account has trustlines and/or offers.
 */
case object AccountMergeHasSubEntries extends AccountMergeResult(-4)

/**
 * AccountMerge operation failed because it would be possible to recreate it with an earlier sequence number.
 */
case object AccountMergeSeqNumTooFar extends AccountMergeResult(-5)

/**
 * AccountMerge operation failed because the resulting destination account balance would be too large.
 */
case object AccountMergeDestinationFull extends AccountMergeResult(-6)
