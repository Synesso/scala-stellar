package stellar.protocol

import net.i2p.crypto.eddsa.{EdDSAPrivateKey, KeyPairGenerator}
import okio.ByteString
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class SeedSpec extends Specification with ScalaCheck {
  import Seeds._

  "seed" should {
    "encode to string" >> prop { seed: Seed =>
      Seed(seed.encodeToString) mustEqual seed
    }

    "provide paired accountId" >> prop { seed: Seed =>
      // TODO (jem) - For now we only check that it looks OK. But we should test the properties fully.
      seed.accountId.encodeToString must beMatching("G[A-D][A-Z0-9]{54}")
    }
  }
}

object Seeds {
  private val genKeyPair = Gen.const(() => new KeyPairGenerator().generateKeyPair()).map(_.apply())
  private val genPrivateKey = genKeyPair.map(_.getPrivate.asInstanceOf[EdDSAPrivateKey])

  val genSeed: Gen[Seed] = genPrivateKey.map(_.getAbyte).map(new ByteString(_)).map(Seed(_))
  implicit val arbSeed: Arbitrary[Seed] = Arbitrary(genSeed)
}
