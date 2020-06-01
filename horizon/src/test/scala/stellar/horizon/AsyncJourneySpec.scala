package stellar.horizon

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.protocol.AccountId

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * Top level tests that demonstrate how to use the blocking endpoints.
 */
class AsyncJourneySpec(implicit ee: ExecutionEnv) extends Specification {

  "client software" should {
    "be able to fetch account details" >> {

      val horizon = Horizon.async()
      val accountId = AccountId("GBRAZP7U3SPHZ2FWOJLHPBO3XABZLKHNF6V5PUIJEEK6JEBKGXWD2IIE")

      val accountDetail: Future[AccountDetail] = horizon.accounts.accountDetail(accountId)

      accountDetail.map(_.id) must beEqualTo(accountId).await(0, 10.seconds)
    }
  }

}
