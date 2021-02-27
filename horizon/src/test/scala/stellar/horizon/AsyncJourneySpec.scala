package stellar.horizon

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.event.AccountCreated
import stellar.horizon.io.HttpOperations.NotFound
import stellar.protocol.{AccountId, Lumen, Seed}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
 * Top level tests that demonstrate how to use the async endpoints.
 */
class AsyncJourneySpec(implicit ee: ExecutionEnv) extends Specification {

  private val FriendBotAccountId = AccountId("GAIH3ULLFQ4DGSECF2AR555KZ4KNDGEKN4AFI4SU2M7B43MGK3QJZNSR")

  "client software" should {

    "be able to fetch horizon instance capabilities" >> {
      val horizon = Horizon.async()
      val state: Future[HorizonState] = horizon.meta.state
      state.map(_.friendbotUrl) must beNone.await(0, 10.seconds)
      state.map(_.networkPassphrase) must beEqualTo("Public Global Stellar Network ; September 2015").await
    }

    "be able to create a new account from a faucet (friendbot), if one is available" >> {
      val horizon = Horizon.async(Horizon.Networks.Test)
      val accountId = AccountId.random
      horizon.friendbot.create(accountId) must beLike[TransactionResponse] { response =>
        response.operationEvents mustEqual List(
          AccountCreated(
            accountId = accountId,
            startingBalance = Lumen(10_000).units,
            fundingAccountId = FriendBotAccountId
          )
        )
        response.feeCharged.units must beGreaterThanOrEqualTo(100L)
      }.await(0, 10.seconds)
    }

    "fail to create a new account from a faucet (friendbot), if none is available" >> {
      val horizon = Horizon.async(Horizon.Networks.Main)
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

    // TODO - Pay with a specified source and sequence number.
    // TODO - maintain a local sequence number?

    "be able to transact a payment" >> {
      val horizon = Horizon.async(Horizon.Networks.Test)

      // TODO (jem) - Test account creation should be performed once up-front.
      val from = Seed.random
      val to = Seed.random

      val createFrom = horizon.friendbot.create(from.accountId)
      val createTo = horizon.friendbot.create(to.accountId)
      Await.ready(createFrom.flatMap(_ => createTo), 1.minute)

      val response = horizon.transact(from).pay(
        sender = from.address,
        recipient = to.address,
        amount = Lumen(100)
      )

      // TODO (jem) - Better assertions
      response must beAnInstanceOf[TransactionResponse].await(0, 10.seconds)
    }
  }

}
