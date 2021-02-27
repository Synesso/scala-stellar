package stellar.horizon.json

import org.json4s.{DefaultFormats, Formats}
import org.json4s.native.JsonMethods.parse
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.{Asset, Lumen, Token}

class AssetReaderSpec extends Specification with ScalaCheck {
  import AssetReaderSpec._
  import stellar.protocol.Assets._
  implicit val formats: Formats = DefaultFormats + AssetReader

  "assets" should {
    "deserialise from json" >> prop { asset: Asset =>
      parse(asJsonDoc(asset)).extract[Asset] mustEqual asset
    }
  }
}

object AssetReaderSpec {
  def asJsonDoc(asset: Asset): String = asset match {
    case Lumen => """{"asset_type":"native"}"""
    case Token(code, issuer) =>
      s"""
         |{
         |  "asset_type":"credit_alphanum${if (code.length <= 4) "4" else "12"}",
         |  "asset_code":"$code",
         |  "asset_issuer":"${issuer.encodeToString}"
         |}""".stripMargin
  }
}
