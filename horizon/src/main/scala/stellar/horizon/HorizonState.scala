package stellar.horizon

import okhttp3.HttpUrl

/**
 * The configuration and status of a Horizon instance.
 *
 * @param version the Horizon software version
 * @param coreVersion the Core software version
 * @param ingestLatestLedger the id of the most recent ledger ingested by this Horizon instance
 * @param historyLatestLedger the id of the most recent ledger in the history
 * @param historyEldestLedger the id of the earliest ledger in the history
 * @param coreLatestLedger the id of the most recent ledger seen by this network
 * @param networkPassphrase the phrase that uniquely identifies this network
 * @param currentProtocolVersion the version of the Stellar protocol currently running
 * @param coreSupportedProtocolVersion the maximum version of the Stellar protocol supported
 * @param friendbotUrl the url to access this network's FriendBot, should it exist.
 */
case class HorizonState(
  version: String,
  coreVersion: String,
  ingestLatestLedger: Long,
  historyLatestLedger: Long,
  historyEldestLedger: Long,
  coreLatestLedger: Long,
  networkPassphrase: String, // TODO - NetworkId
  currentProtocolVersion: Int,
  coreSupportedProtocolVersion: Int,
  friendbotUrl: Option[HttpUrl]
)
