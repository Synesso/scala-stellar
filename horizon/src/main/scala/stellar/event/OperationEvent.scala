package stellar.event

import stellar.protocol.AccountId

sealed trait OperationEvent

/**
 * Account was created.
 */
case class AccountCreated(
  accountId: AccountId,
  startingBalance: Long,
  fundingAccountId: AccountId
) extends OperationEvent

