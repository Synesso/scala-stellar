package stellar.horizon.json

import org.json4s.JsonAST.JObject
import org.json4s.{DefaultFormats, Formats}
import stellar.horizon.TransactionResponse

object TransactionResponseReader extends JsReader[TransactionResponse]({ o: JObject =>
  implicit val formats: Formats = DefaultFormats

  TransactionResponse(
    (o \ "hash").extract[String],
    (o \ "ledger").extract[Long],
    (o \ "envelope_xdr").extract[String],
    (o \ "result_xdr").extract[String],
    (o \ "result_meta_xdr").extract[String]
  )
})

