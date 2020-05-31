package stellar.horizon.json

import org.json4s.{DefaultFormats, Formats, JObject}
import stellar.horizon.AuthFlags

object AuthFlagsReader extends JsReader[AuthFlags]({ o: JObject =>
  implicit val formats: Formats = DefaultFormats

  AuthFlags(
    (o \ "auth_required").extract[Boolean],
    (o \ "auth_revocable").extract[Boolean],
    (o \ "auth_immutable").extract[Boolean])
})
