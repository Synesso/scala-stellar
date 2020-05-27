package stellar.horizon

import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats}
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import scala.util.Random

class BalanceSpec extends Specification with ScalaCheck {
  import BalanceSpec._

  "balance" should {
    "deserialise from json" >> prop { balance: Balance =>
      parse(asJsonDoc(balance)).extract[Balance] mustEqual balance
    }
  }
}

object BalanceSpec {
  import stellar.horizon.json.AmountReaderSpec
  import stellar.protocol.Amounts._

  val genBalance: Gen[Balance] = for {
    amount <- genAmount
    limit <- Gen.option(Gen.posNum[Long])
    buyingLiabilities <- Gen.posNum[Long]
    sellingLiabilities <- Gen.posNum[Long]
    authorized <- Gen.oneOf(true, false)
    authorizedToMaintainLiabilities <- Gen.oneOf(true, false)
  } yield Balance(amount, limit, buyingLiabilities, sellingLiabilities, authorized, authorizedToMaintainLiabilities)

  implicit val arbBalance: Arbitrary[Balance] = Arbitrary(genBalance)

  implicit val formats: Formats = DefaultFormats + BalanceReader

  def asJsonDoc(balance: Balance): String =
    s"""
       |{
       |  ${balance.limit.map(limit => s""""limit": "${toDecimalString(limit)}",""").getOrElse("")}
       |  "buying_liabilities": "${toDecimalString(balance.buyingLiabilities)}",
       |  "selling_liabilities": "${toDecimalString(balance.sellingLiabilities)}",
       |  ${maybeOmitWhenFalse("is_authorized", balance.authorized)}
       |  ${maybeOmitWhenFalse("is_authorized_to_maintain_liabilities", balance.authorizedToMaintainLiabilities)}
       |  ${AmountReaderSpec.asJsonDoc(balance.amount).replaceAll("[{}]", "").trim}
       |}""".stripMargin

  private def maybeOmitWhenFalse(key: String, value: Boolean): String =
    if (!value && Random.nextBoolean()) "" else s""""$key":$value,"""

  private def toDecimalString(long: Long): String =
    (BigDecimal(long) / BigDecimal(10_000_000)).bigDecimal.toPlainString
}