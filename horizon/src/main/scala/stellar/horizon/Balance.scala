package stellar.horizon

import stellar.protocol.Amount

/**
 * The current balance of, and the restrictions upon, a specific asset and account.
 *
 * @param amount                          the current balance
 * @param limit                           the maximum quantity of units that the account is willing to accept
 * @param buyingLiabilities               the current liabilities aggregated across all open buy orders from this account
 * @param sellingLiabilities              the current liabilities aggregated across all open sell orders from this account
 * @param authorized                      If true, the account can send, receive, buy and sell this asset.
 * @param authorizedToMaintainLiabilities If true, the account can maintain offers to buy and sell this asset, but not send or receive.
 */
case class Balance(amount: Amount,
                   limit: Option[Long],
                   buyingLiabilities: Long,
                   sellingLiabilities: Long,
                   authorized: Boolean,
                   authorizedToMaintainLiabilities: Boolean)

