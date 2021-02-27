package stellar.protocol.ledger

import cats.data.State
import stellar.protocol.xdr.Encode.bytes
import stellar.protocol.xdr.{Decoder, Encodable}

/**
 * The thresholds for operations on this account, as described in transaction meta data for ledger effects.
 *
 * @param master The weight provided by the primary signature for this account.
 * @param low The weight required for a valid transaction including the Allow Trust and Bump Sequence operations.
 * @param med The weight required for a valid transaction including the Create Account, Payment, Path Payment, Manage
 *            Buy Offer, Manage Sell Offer, Create Passive Sell Offer, Change Trust, Inflation, and Manage Data operations.
 * @param high The weight required for a valid transaction including the Account Merge and Set Options operations.
 */
case class LedgerThreshold(master: Int, low: Int, med: Int, high: Int) extends Encodable {
  override def encode: LazyList[Byte] = bytes(4, Array[Byte](master.toByte, low.toByte, med.toByte, high.toByte))
}

object LedgerThreshold extends Decoder[LedgerThreshold] {
  val decodeOld: State[Seq[Byte], LedgerThreshold] = bytes(4).map { bs =>
    val Seq(master, low, med, high): Seq[Int] = bs.map(_ & 0xff)
    LedgerThreshold(master, low, med, high)
  }
}