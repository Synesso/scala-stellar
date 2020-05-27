package stellar.horizon

import org.json4s.{DefaultFormats, Formats, JObject}
import stellar.horizon.json.{AmountReader, JsReader}
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

object BalanceReader extends JsReader[Balance]({ o: JObject =>
  import JsReader._
  implicit val formats: Formats = DefaultFormats + new AmountReader("balance")

  Balance(
    amount = o.extract[Amount],
    limit = optDoubleStringToLong("limit", o),
    buyingLiabilities = doubleStringToLong("buying_liabilities", o),
    sellingLiabilities = doubleStringToLong("selling_liabilities", o),
    authorized = (o \ "is_authorized").extractOpt[Boolean].getOrElse(false),
    authorizedToMaintainLiabilities = (o \ "is_authorized_to_maintain_liabilities").extractOpt[Boolean].getOrElse(false)
  )
})
