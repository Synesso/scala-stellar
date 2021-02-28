package stellar.event

import org.stellar.xdr.{Int64, MuxedAccount, PaymentOp}
import stellar.protocol.Address

/**
 * Account was closed/merged.
 */
case class AccountMerged(
  override val source: Address,
  to: Address,
  amount: Long
) extends MergeAccountEvent

object AccountMerged {
  def decode(
    source: MuxedAccount,
    destination: MuxedAccount,
    amount: Int64
  ): AccountMerged = AccountMerged(
    source = Address.decode(source),
    to = Address.decode(destination),
    amount = amount.getInt64
  )
}