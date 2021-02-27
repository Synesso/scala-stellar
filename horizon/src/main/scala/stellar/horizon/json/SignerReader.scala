package stellar.horizon.json

import org.json4s.{DefaultFormats, Formats, JObject}
import stellar.protocol.{AccountId, Asset, HashX, Lumen, PreAuthTx, Signer, PresentableSignerKey, Token}

object SignerReader extends JsReader[Signer]({ o: JObject =>
  implicit val formats: Formats = DefaultFormats

  val keyString = (o \ "key").extract[String]
  val signerKey = (o \ "type").extract[String] match {
    case "ed25519_public_key" => AccountId(keyString)
    case "sha256_hash" => HashX(keyString)
    case "preauth_tx" => PreAuthTx(keyString)
  }
  val weight = (o \ "weight").extract[Int]
  Signer(signerKey, weight)
})
