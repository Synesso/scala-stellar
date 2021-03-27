package stellar.event
import org.stellar.xdr.{ChangeTrustOp, ChangeTrustResultCode, MuxedAccount}
import stellar.protocol.Address

case class TrustChangeFailed(
  override val source: Address,
  failure: TrustChangeFailed.EnumVal
) extends TrustChangeEvent {
  override val accepted: Boolean = false
}

object TrustChangeFailed {
  sealed trait EnumVal
  case object IssuerDoesNotExist extends EnumVal

  private val failureTypes = Map(
    ChangeTrustResultCode.CHANGE_TRUST_NO_ISSUER -> IssuerDoesNotExist
  )

  def decode(
    op: ChangeTrustOp,
    source: MuxedAccount,
    failure: ChangeTrustResultCode
  ): TrustChangeEvent = TrustChangeFailed(
    source = Address.decode(source),
    failure = failureTypes(failure)
  )
}
