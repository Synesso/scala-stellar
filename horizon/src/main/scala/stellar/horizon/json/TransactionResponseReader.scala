package stellar.horizon.json

import org.json4s.JsonAST.JObject
import org.json4s.{DefaultFormats, Formats}
import stellar.horizon.TransactionResponse

object TransactionResponseReader extends JsReader[TransactionResponse]({ o: JObject =>
  implicit val formats: Formats = DefaultFormats

  val xdrObj = (o \ "successful").extractOpt[Boolean] match {
    case Some(true) => o
    case _ => o \ "extras"
  }
  TransactionResponse(
    (xdrObj \ "envelope_xdr").extract[String],
    (xdrObj \ "result_xdr").extract[String],
  )
})

