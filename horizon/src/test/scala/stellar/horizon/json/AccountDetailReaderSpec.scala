package stellar.horizon.json

import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME
import java.time.{Instant, ZoneId}

import okio.ByteString
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization
import org.json4s.{Formats, NoTypeHints}
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.horizon.{AccountDetail, AuthFlags, Thresholds}

class AccountDetailSpec extends Specification with ScalaCheck {
  import AccountDetails._

  "account detail" should {
    "deserialise from json" >> prop { accountDetail: AccountDetail =>
      parse(asJsonDoc(accountDetail)).extract[AccountDetail] mustEqual accountDetail
    }
  }
}

object AccountDetails {
  import stellar.horizon.json.BalanceReaderSpec._
  import stellar.protocol.AccountIds._
  import stellar.protocol.Signers._

  val genAccountDetail: Gen[AccountDetail] = for {
    accountId <- genAccountId
    sequence <- Gen.posNum[Long]
    lastModifiedLedger <- Gen.posNum[Long]
    lastModifiedTime <- Gen.posNum[Long].map(Instant.ofEpochMilli)
      .map(_.atZone(ZoneId.of("Z")).withNano(0))
    subEntryCount <- Gen.posNum[Int]
    thresholds <- Gen.listOfN(3, Gen.posNum[Int]).map { case List(l, m, h) => Thresholds(l, m, h) }
    authFlags <- Gen.listOfN(3, Gen.oneOf(false, true)).map { case List(a, b, c) => AuthFlags(a, b, c) }
    balances <- Gen.listOf(genBalance)
    signers <- Gen.listOf(genSigner)
    data <- Gen.mapOf(for {
      key <- Gen.identifier
      value <- Gen.alphaNumStr.map(v => new ByteString(v.getBytes("UTF-8")))
    } yield key -> value)
  } yield AccountDetail(accountId, sequence, lastModifiedLedger, lastModifiedTime, subEntryCount,
    thresholds, authFlags, balances, signers, data)

  implicit val arbAccountDetail: Arbitrary[AccountDetail] = Arbitrary(genAccountDetail)

  implicit val formats: Formats = Serialization.formats(NoTypeHints) + AccountDetailReader

  def asJsonDoc(detail: AccountDetail): String = {
    val id = detail.id.encodeToString
    s"""
      |{
      |  "id": "$id",
      |  "account_id": "$id",
      |  "sequence": "${detail.sequence}",
      |  "subentry_count": ${detail.subEntryCount},
      |  "last_modified_ledger": ${detail.lastModifiedLedger},
      |  "last_modified_time": "${detail.lastModifiedTime.format(ISO_ZONED_DATE_TIME)}",
      |  "thresholds": {
      |    "low_threshold": ${detail.thresholds.low},
      |    "med_threshold": ${detail.thresholds.med},
      |    "high_threshold": ${detail.thresholds.high}
      |  },
      |  "flags": {
      |    "auth_required": ${detail.authFlags.required},
      |    "auth_revocable": ${detail.authFlags.revocable},
      |    "auth_immutable": ${detail.authFlags.immutable}
      |  },
      |  "balances": [
      |    ${detail.balances.map(BalanceReaderSpec.asJsonDoc).mkString(",")}
      |  ],
      |  "signers": [
      |    ${detail.signers.map(SignerReaderSpec.asJsonDoc).mkString(",")}
      |  ],
      |  "data": {
      |    ${detail.data.map { case (k, v) => s""""$k":"${v.base64()}""""}.mkString(",")}
      |  }
      |}""".stripMargin
  }

}