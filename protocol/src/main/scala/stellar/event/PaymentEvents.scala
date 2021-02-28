package stellar.event

import org.stellar.xdr.{MuxedAccount, PaymentOp}
import stellar.protocol.{Address, Amount}

/**
 * Payment was completed.
 */
case class PaymentMade(
  override val source: Address,
  to: Address,
  amount: Amount
) extends PaymentEvent

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