package stellar.horizon

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.horizon.io.HttpOperations.NotFound
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

    "be able to create a new account from a faucet (friendbot), if one is available" >> {
      val horizon = Horizon.async(Horizon.Endpoints.Test)
      val accountId = AccountId.random
      val response = horizon.friendbot.create(accountId)
      // TODO (jem) - When we can transact, make sure to roll the created account back in.
      response must beAnInstanceOf[TransactionResponse].await(0, 10.seconds)
    }

    "fail to create a new account from a faucet (friendbot), if none is available" >> {
      val horizon = Horizon.async(Horizon.Endpoints.Main)
      val accountId = AccountId.random
      val response = horizon.friendbot.create(accountId)
      response must throwA[NotFound].await(0, 10.seconds)
    }

    "be able to fetch account details" >> {
      val horizon = Horizon.async()
      val accountId = AccountId("GBRAZP7U3SPHZ2FWOJLHPBO3XABZLKHNF6V5PUIJEEK6JEBKGXWD2IIE")
      val accountDetail: Future[AccountDetail] = horizon.account.detail(accountId)
      accountDetail.map(_.id) must beEqualTo(accountId).await(0, 10.seconds)
    }
  }

}
