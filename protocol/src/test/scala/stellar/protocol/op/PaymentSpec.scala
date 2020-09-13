package stellar.protocol.op

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.Addresses.genAddress
import stellar.protocol.Amounts.genAmount
import stellar.protocol.XdrSerdeMatchers

class PaymentSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import Payments._

  "payment" should {
    "encode and decode" >> prop { payment: Payment =>
      payment should xdrDecodeAndEncode(Payment)
    }
  }
}

object Payments {
  val genPayment: Gen[Payment] = for {
    sender <- Gen.option(genAddress)
    recipient <- genAddress
    amount <- genAmount
  } yield Payment(sender, recipient, amount)
  implicit val arbPayment: Arbitrary[Payment] = Arbitrary(genPayment)
}