package stellar.horizon.json

import org.json4s.{DefaultFormats, Formats, JObject}
import stellar.horizon.Balance
import stellar.protocol.Amount

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
