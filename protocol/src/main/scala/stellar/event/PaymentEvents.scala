package stellar.event

import org.stellar.xdr.{MuxedAccount, PaymentOp, PaymentResultCode}
import stellar.protocol.{Address, Amount}

/**
 * Payment was completed.
 */
case class PaymentMade(
  override val source: Address,
  to: Address,
  amount: Amount
) extends PaymentEvent {
  override val accepted: Boolean = true
}

object PaymentMade {
  def decode(
    op: PaymentOp,
    source: MuxedAccount
  ): PaymentMade = {
    PaymentMade(
      source = Address.decode(source),
      to = Address.decode(op.getDestination),
      amount = Amount.decode(op.getAsset, op.getAmount)
    )
  }
}

case class PaymentFailed(
  override val source: Address,
  to: Address,
  amount: Amount,
  failure: PaymentFailed.EnumVal
) extends PaymentEvent {
  override val accepted: Boolean = false
}

object PaymentFailed {
  sealed trait EnumVal
  case object InsufficientFunds extends EnumVal

  private val failureTypes = Map(
    PaymentResultCode.PAYMENT_UNDERFUNDED -> InsufficientFunds
  )

  def decode(
    op: PaymentOp,
    source: MuxedAccount,
    failure: PaymentResultCode
  ) = PaymentFailed(
      source = Address.decode(source),
      to = Address.decode(op.getDestination),
      amount = Amount.decode(op.getAsset, op.getAmount),
      failure = failureTypes(failure)
    )
}
