package stellar.horizon

import okhttp3.HttpUrl

/** The configuration and status of a Horizon instance */
case class HorizonState(
  version: String,
  coreVersion: String,
  ingestLatestLedger: Long,
  historyLatestLedger: Long,
  historyEldestLedger: Long,
  coreLatestLedger: Long,
  networkPassphrase: String,
  currentProtocolVersion: Int,
  coreSupportedProtocolVersion: Int,
  friendbotUrl: Option[HttpUrl]
)
