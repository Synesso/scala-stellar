package stellar.horizon.json

import org.json4s.{DefaultFormats, Formats, JObject}
import stellar.protocol.{AccountId, Asset, Lumen, Token}

object AssetReader extends JsReader[Asset]({ o: JObject =>
  implicit val formats: Formats = DefaultFormats

  (o \ "asset_type").extract[String] match {
    case "native" => Lumen
    case "credit_alphanum4" | "credit_alphanum12" =>
      Token(
        code = (o \ "asset_code").extract[String],
        issuer = AccountId((o \ "asset_issuer").extract[String])
      )
  }

})
