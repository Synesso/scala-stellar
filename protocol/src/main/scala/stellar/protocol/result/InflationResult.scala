package stellar.protocol.result

import cats.data.State
import stellar.protocol.AccountId
import stellar.protocol.xdr.Encode.{arr, int, long}
import stellar.protocol.xdr.{Decoder, Encodable}

/**
 * The result of an Inflation operation. Note that Inflation is permanently disabled. This type
 * remains to cater for parsing of historical results.
 */
sealed trait InflationResult extends OpResult {
  val opCode: Int = 9 // TODO (jem) - this field will have to belong to all result types
}

case object InflationNotDue extends InflationResult {
  override def encode: LazyList[Byte] = int(-1)
}

case class InflationSuccess(payouts: List[InflationPayout]) extends InflationResult {
  override def encode: LazyList[Byte] = int(0) ++ arr(payouts)
}

object InflationResult extends Decoder[InflationResult] {
  override val decodeOld: State[Seq[Byte], InflationResult] = int.flatMap {
    case 0 => arr(InflationPayout.decodeOld).map(InflationSuccess)
    case -1 => State.pure(InflationNotDue)
  }
}

case class InflationPayout(recipient: AccountId, units: Long) extends Encodable {
  def encode: LazyList[Byte] = recipient.encode ++ long(units)
}

object InflationPayout extends Decoder[InflationPayout] {
  val decodeOld: State[Seq[Byte], InflationPayout] = for {
    recipient <- AccountId.decodeOld
    units <- long
  } yield InflationPayout(recipient, units)
}
