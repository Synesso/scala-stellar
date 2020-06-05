package stellar.horizon.json

import okhttp3.HttpUrl
import org.json4s.{DefaultFormats, Formats}
import org.json4s.native.JsonMethods.parse
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.horizon.{AccountDetail, HorizonState}

class HorizonStateReaderSpec extends Specification with ScalaCheck {
  import HorizonStates._

  "horizon state" should {
    "deserialise from json" >> prop { state: HorizonState =>
      parse(asJsonDoc(state)).extract[HorizonState] mustEqual state
    }
  }
}

object HorizonStates {

  val genHorizonState: Gen[HorizonState] = for {
    version <- Gen.identifier
    coreVersion <- Gen.identifier
    ingestLatestLedger <- Gen.posNum[Long]
    historyLatestLedger <- Gen.posNum[Long]
    historyEldestLedger <- Gen.posNum[Long]
    coreLatestLedger <- Gen.posNum[Long]
    networkPassphrase <- Gen.identifier
    currentProtocolVersion <- Gen.posNum[Int]
    coreSupportedProtocolVersion <- Gen.posNum[Int]
    friendbotUrl <- Gen.option(Gen.const(HttpUrl.parse("https://friendbot.stellar.org/")))
  } yield HorizonState(version, coreVersion, ingestLatestLedger, historyLatestLedger, historyEldestLedger,
    coreLatestLedger, networkPassphrase, currentProtocolVersion, coreSupportedProtocolVersion, friendbotUrl)

  implicit val arbHorizonState: Arbitrary[HorizonState] = Arbitrary(genHorizonState)

  implicit val formats: Formats = DefaultFormats + HorizonStateReader

  def asJsonDoc(state: HorizonState): String = {
    val friendbot = state.friendbotUrl
      .map(url => s""""friendbot":{"href":"$url{?addr}","templated":true}""")
      .getOrElse("")
    s"""{
       |  "_links": { $friendbot },
       |  "horizon_version": "${state.version}",
       |  "core_version": "${state.coreVersion}",
       |  "ingest_latest_ledger": ${state.ingestLatestLedger},
       |  "history_latest_ledger": ${state.historyLatestLedger},
       |  "history_elder_ledger": ${state.historyEldestLedger},
       |  "core_latest_ledger": ${state.coreLatestLedger},
       |  "network_passphrase": "${state.networkPassphrase}",
       |  "current_protocol_version": ${state.currentProtocolVersion},
       |  "core_supported_protocol_version": ${state.coreSupportedProtocolVersion}
       |}""".stripMargin
  }
}