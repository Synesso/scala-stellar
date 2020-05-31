package stellar.horizon.json

import org.json4s.{DefaultFormats, Formats, JObject}
import stellar.horizon.Thresholds

object ThresholdsReader extends JsReader[Thresholds]({ o: JObject =>
  implicit val formats: Formats = DefaultFormats

  Thresholds(
    (o \ "low_threshold").extract[Int],
    (o \ "med_threshold").extract[Int],
    (o \ "high_threshold").extract[Int])
})
