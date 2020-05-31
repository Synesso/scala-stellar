package stellar.horizon.json

import java.time.ZonedDateTime

import okio.ByteString
import org.json4s.{DefaultFormats, Formats}
import org.json4s.JsonAST.JObject
import stellar.horizon.{AccountDetail, AuthFlags, Balance, Thresholds}
import stellar.protocol.{AccountId, Signer}

object AccountDetailReader extends JsReader[AccountDetail]({ o: JObject =>
  implicit val formats: Formats = DefaultFormats + ThresholdsReader + AuthFlagsReader + BalanceReader + SignerReader

  val id = AccountId((o \ "id").extract[String])
  val seq = (o \ "sequence").extract[String].toLong
  val lastModifiedLedger = (o \ "last_modified_ledger").extract[Long]
  val lastModifiedTime = ZonedDateTime.parse((o \ "last_modified_time").extract[String])
  val subEntryCount = (o \ "subentry_count").extract[Int]
  val thresholds = (o \ "thresholds").extract[Thresholds]
  val flags = (o \ "flags").extract[AuthFlags]
  val balances = (o \ "balances").extract[List[Balance]]
  val signers = (o \ "signers").extract[List[Signer]]
  val data = (o \ "data").extract[Map[String, String]].view.mapValues(ByteString.decodeBase64).toMap

  AccountDetail(id, seq, lastModifiedLedger, lastModifiedTime, subEntryCount, thresholds, flags, balances, signers, data)
})

