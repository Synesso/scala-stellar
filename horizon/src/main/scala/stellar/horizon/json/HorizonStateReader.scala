package stellar.horizon.json

import okhttp3.HttpUrl
import org.json4s.{DefaultFormats, JObject}
import stellar.horizon.HorizonState

object HorizonStateReader extends JsReader[HorizonState]({ o: JObject =>
  implicit val formats = DefaultFormats

  val friendbotUrl = (o \ "_links" \ "friendbot" \ "href").extractOpt[String]
      .map(_.replaceAll("\\{.*\\}", ""))
      .map(HttpUrl.parse)
  HorizonState(
    version = (o \ "horizon_version").extract[String],
    coreVersion = (o \ "core_version").extract[String],
    ingestLatestLedger = (o \ "ingest_latest_ledger").extract[Long],
    historyLatestLedger = (o \ "history_latest_ledger").extract[Long],
    historyEldestLedger = (o \ "history_elder_ledger").extract[Long],
    coreLatestLedger = (o \ "core_latest_ledger").extract[Long],
    networkPassphrase = (o \ "network_passphrase").extract[String],
    currentProtocolVersion = (o \ "current_protocol_version").extract[Int],
    coreSupportedProtocolVersion = (o \ "core_supported_protocol_version").extract[Int],
    friendbotUrl = friendbotUrl
  )
})
