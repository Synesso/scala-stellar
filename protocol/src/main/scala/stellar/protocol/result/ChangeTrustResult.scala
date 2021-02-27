package stellar.protocol.result

import cats.data.State
import stellar.protocol.xdr.Decoder
import stellar.protocol.xdr.Encode.int

sealed abstract class ChangeTrustResult(val opResultCode: Int) extends OpResult {
  override val opCode: Int = 6
  override def encode: LazyList[Byte] = int(opResultCode)
}

object ChangeTrustResult extends Decoder[ChangeTrustResult] {
  val decodeOld: State[Seq[Byte], ChangeTrustResult] = int.map {
    case 0 => ChangeTrustSuccess
    case -1 => ChangeTrustMalformed
    case -2 => ChangeTrustNoIssuer
    case -3 => ChangeTrustInvalidLimit
    case -4 => ChangeTrustLowReserve
    case -5 => ChangeTrustSelfNotAllowed
  }
}
/**
 * ChangeTrust operation was successful.
 */
case object ChangeTrustSuccess extends ChangeTrustResult(0)

/**
 * ChangeTrust operation failed because the request was malformed.
 * E.g. The limit was less than zero, or the asset was malformed, or the native asset was provided.
 */
case object ChangeTrustMalformed extends ChangeTrustResult(-1)

/**
 * ChangeTrust operation failed because the issuer account does not exist.
 */
case object ChangeTrustNoIssuer extends ChangeTrustResult(-2)

/**
 * ChangeTrust operation failed because the limit was zero or less than the current balance.
 */
case object ChangeTrustInvalidLimit extends ChangeTrustResult(-3)

/**
 * ChangeTrust operation failed because there is not enough funds in reserve to create a new trustline.
 */
case object ChangeTrustLowReserve extends ChangeTrustResult(-4)

/**
 * ChangeTrust operation failed because it is not valid to trust your own issued asset.
 */
case object ChangeTrustSelfNotAllowed extends ChangeTrustResult(-5)