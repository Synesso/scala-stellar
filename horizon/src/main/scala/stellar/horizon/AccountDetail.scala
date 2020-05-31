package stellar.horizon

import java.time.ZonedDateTime

import okio.ByteString
import stellar.protocol.{AccountId, Signer}

case class AccountDetail(id: AccountId,
                         sequence: Long,
                         lastModifiedLedger: Long,
                         lastModifiedTime: ZonedDateTime,
                         subEntryCount: Int,
                         thresholds: Thresholds,
                         authFlags: AuthFlags,
                         balances: List[Balance],
                         signers: List[Signer],
                         data: Map[String, ByteString])

