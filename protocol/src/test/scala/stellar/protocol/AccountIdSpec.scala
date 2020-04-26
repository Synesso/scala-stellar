package stellar.protocol

import net.i2p.crypto.eddsa.{EdDSAPublicKey, KeyPairGenerator}
import okio.ByteString
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class AccountIdSpec extends Specification with ScalaCheck with XdrSerdeMatchers {
  import AccountIds._

  "account id" should {
    "encode and decode" >> prop { accountId: AccountId =>
      accountId must xdrDecodeAndEncode(AccountId)
    }

    "encode to string" >> prop { accountId: AccountId =>
      AccountId(accountId.encodeToString) mustEqual accountId
    }
  }
}

object AccountIds {
  private val genKeyPair = Gen.const(() => new KeyPairGenerator().generateKeyPair()).map(_.apply())
  private val genPublicKey = genKeyPair.map(_.getPublic.asInstanceOf[EdDSAPublicKey])

  val genAccountId: Gen[AccountId] = genPublicKey.map(_.getAbyte).map(new ByteString(_)).map(AccountId(_))
  implicit val arbAccountId: Arbitrary[AccountId] = Arbitrary(genAccountId)
}
