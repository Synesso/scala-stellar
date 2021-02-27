package stellar.protocol.result

import cats.data.State
import stellar.protocol.xdr.Decoder
import stellar.protocol.xdr.Encode.int

sealed abstract class AllowTrustResult(val opResultCode: Int) extends OpResult {
  override val opCode: Int = 7
  override def encode: LazyList[Byte] = int(opResultCode)
}

object AllowTrustResult extends Decoder[AllowTrustResult] {
  val decodeOld: State[Seq[Byte], AllowTrustResult] = int.map {
    case 0 => AllowTrustSuccess
    case -1 => AllowTrustMalformed
    case -2 => AllowTrustNoTrustLine
    case -3 => AllowTrustNotRequired
    case -4 => AllowTrustCannotRevoke
    case -5 => AllowTrustSelfNotAllowed
  }
}

/**
 * AllowTrust operation was successful.
 */
case object AllowTrustSuccess extends AllowTrustResult(0)

/**
 * AllowTrust operation failed because the request was malformed.
 * E.g. The limit was less than zero, or the asset was malformed, or the native asset was provided.
 */
case object AllowTrustMalformed extends AllowTrustResult(-1)

/**
 * AllowTrust operation failed because the trustor does not have a trustline.
 */
case object AllowTrustNoTrustLine extends AllowTrustResult(-2)

/**
 * AllowTrust operation failed because the source account does not require trust.
 */
case object AllowTrustNotRequired extends AllowTrustResult(-3)

/**
 * AllowTrust operation failed because the source account is unable to revoke trust.
 */
case object AllowTrustCannotRevoke extends AllowTrustResult(-4)

/**
 * AllowTrust operation failed because it is not valid to trust your own issued asset.
 */
case object AllowTrustSelfNotAllowed extends AllowTrustResult(-5)
