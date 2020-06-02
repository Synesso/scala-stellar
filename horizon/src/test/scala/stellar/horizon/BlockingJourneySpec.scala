package stellar.horizon

import org.specs2.matcher.Matchers
import org.specs2.mutable.Specification
import stellar.protocol.AccountId

import scala.util.Try

/**
 * Top level tests that demonstrate how to use the blocking endpoints.
 */
class BlockingJourneySpec extends Specification with Matchers {

  "client software" should {
    "be able to fetch account details" >> {
      val horizon = Horizon.sync()
      val accountId = AccountId("GBRAZP7U3SPHZ2FWOJLHPBO3XABZLKHNF6V5PUIJEEK6JEBKGXWD2IIE")

      val accountDetail: Try[AccountDetail] = horizon.account.detail(accountId)

      accountDetail must beSuccessfulTry.like(_.id mustEqual accountId)
    }
  }

}
