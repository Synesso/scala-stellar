package stellar.horizon

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.protocol.AccountId

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * Top level tests that demonstrate how to use the async endpoints.
 */
class AsyncJourneySpec(implicit ee: ExecutionEnv) extends Specification {

  "client software" should {

    "be able to fetch horizon instance capabilities" >> {
      val horizon = Horizon.async()
      val state: Future[HorizonState] = horizon.meta.state
      state.map(_.friendbotUrl) must beNone.await(0, 10.seconds)
      state.map(_.networkPassphrase) must beEqualTo("Public Global Stellar Network ; September 2015").await
    }

    "be able to fetch account details" >> {
      val horizon = Horizon.async()
      val accountId = AccountId("GBRAZP7U3SPHZ2FWOJLHPBO3XABZLKHNF6V5PUIJEEK6JEBKGXWD2IIE")
      val accountDetail: Future[AccountDetail] = horizon.account.detail(accountId)
      accountDetail.map(_.id) must beEqualTo(accountId).await(0, 10.seconds)
    }
  }

}
