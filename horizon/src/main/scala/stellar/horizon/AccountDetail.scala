package stellar.horizon

import java.time.ZonedDateTime

import okio.ByteString
import stellar.protocol.{AccountId, Amount, Asset, Lumen, Signer}

/**
 * The details of a specific account.
 */
case class AccountDetail(
  id: AccountId,
  sequence: Long,
  lastModifiedLedger: Long,
  lastModifiedTime: ZonedDateTime,
  subEntryCount: Int,
  thresholds: Thresholds,
  authFlags: AuthFlags,
  balances: List[Balance],
  signers: List[Signer],
  data: Map[String, ByteString]
) {
  def nextSequence: Long = sequence + 1
  def balance(asset: Asset): Option[Amount] = balances.map(_.amount).find(_.asset == asset)
}

