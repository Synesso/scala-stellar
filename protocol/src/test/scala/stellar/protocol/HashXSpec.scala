package stellar.protocol

import okio.ByteString
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class HashXSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import HashXs._

  "pre authorised transaction" should {
    "encode and decode" >> prop { hashX: HashX =>
      hashX must xdrDecodeAndEncode(HashX)
    }

    "encode to string" >> prop { hashX: HashX =>
      HashX(hashX.encodeToString) mustEqual hashX
    }

    "sign things deterministically" >> prop { (hashX: HashX, data: ByteString) =>
      val signature = hashX.sign(data)
      hashX.sign(data) mustEqual signature
      signature.data mustEqual data
      signature.hint.size() mustEqual 4
    }
  }
}

object HashXs {
  val arbByte: Gen[Byte] = Arbitrary.arbByte.arbitrary
  val genHashX: Gen[HashX] = Gen.containerOfN[Array, Byte](32, arbByte)
    .map(bs => HashX(new ByteString(bs)))
  implicit val arbByteString: Arbitrary[ByteString] = Arbitrary(
    Gen.containerOf[Array, Byte](arbByte).map(bs => new ByteString(bs))
  )
  implicit val arbAccountId: Arbitrary[HashX] = Arbitrary(genHashX)
}
