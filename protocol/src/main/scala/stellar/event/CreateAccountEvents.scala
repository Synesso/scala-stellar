package stellar.event

import org.stellar.xdr.{CreateAccountOp, MuxedAccount}
import stellar.protocol.{AccountId, Address}


/**
 * Account was created.
 */
case class AccountCreated(
  accountId: AccountId,
  startingBalance: Long,
  source: Address
) extends CreateAccountEvent {
  override val accepted: Boolean = true
}

object AccountCreated {

  def decode(op: CreateAccountOp, source: MuxedAccount): AccountCreated = {
    AccountCreated(
      accountId = AccountId.decode(op.getDestination.getAccountID),
      startingBalance = op.getStartingBalance.getInt64,
      source = Address.decode(source)
    )
  }
}
