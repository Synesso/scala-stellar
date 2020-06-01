package stellar.horizon

import org.specs2.mutable.Specification
import stellar.protocol.AccountId

import scala.util.Try

/**
 * Top level tests that demonstrate how to use the blocking endpoints.
 */
class BlockingJourneySpec extends Specification {

  "client software" should {
    "be able to fetch account details" >> {

//      val horizon = Horizons.SdfMainNet.Blocking
//      val accountId = AccountId("GBRAZP7U3SPHZ2FWOJLHPBO3XABZLKHNF6V5PUIJEEK6JEBKGXWD2IIE")
//
//      val accountDetail: Try[AccountDetail] = horizon.accountDetail(accountId)
//
//      accountDetail must beSuccessfulTry.like(_.id mustEqual accountId)
      1 mustEqual 10
    }
  }

}
