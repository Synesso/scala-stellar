package stellar.protocol.ledger

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import stellar.protocol.xdr.Encode

class IssuerFlagSpec extends Specification with ScalaCheck {
  import IssuerFlags._

  "issuer flag sets" should {
    "be deserialisable" >> prop { flags: Set[IssuerFlag] =>
      val encoded = Encode.int(flags.map(_.i + 0).fold(0)(_ | _))
      val (remaining, value) = IssuerFlagSet.decodeOld.run(encoded).value
      remaining must beEmpty
      value mustEqual flags
    }
  }
}

object IssuerFlags {
  val genIssuerFlags: Gen[Set[IssuerFlag]] = Gen.containerOf[Set, IssuerFlag](
    Gen.oneOf(AuthorizationRequiredFlag, AuthorizationImmutableFlag, AuthorizationImmutableFlag))
  implicit val arbIssuerFlags: Arbitrary[Set[IssuerFlag]] = Arbitrary(genIssuerFlags)
}