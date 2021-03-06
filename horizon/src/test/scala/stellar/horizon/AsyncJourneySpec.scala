package stellar.horizon

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.event.{AccountCreated, PaymentMade}
import stellar.horizon.io.HttpOperations.NotFound
import stellar.protocol.op.{CreateAccount, Pay}
import stellar.protocol.{AccountId, Lumen, Seed, Transaction}

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
      val accountId = Seed.random.accountId
      val response = horizon.friendbot.create(accountId)
      response must beLike[TransactionResponse] { res =>
        res.operationEvents mustEqual List(
          AccountCreated(
            accountId = accountId,
            startingBalance = Lumen(10_000).units,
            fundingAccountId = FriendBotAccountId
          )
        )
        res.feeCharged.units must beGreaterThanOrEqualTo(100L)
      }.await(0, 30.seconds)
    }

    "fail to create a new account from a faucet (friendbot), if none is available" >> {
      val horizon = Horizon.async(Horizon.Networks.Main)
      val accountId = Seed.random.accountId
      val response = horizon.friendbot.create(accountId)
      response must throwA[NotFound].await(0, 10.seconds)
    }

    "be able to fetch account details" >> {
      val horizon = Horizon.async()
      val accountId = AccountId("GBRAZP7U3SPHZ2FWOJLHPBO3XABZLKHNF6V5PUIJEEK6JEBKGXWD2IIE")
      val accountDetail: Future[AccountDetail] = horizon.account.detail(accountId)
      accountDetail.map(_.id) must beEqualTo(accountId).await(0, 10.seconds)
    }

    "be able to create a new account" >> {
      val horizon = Horizon.async(Horizon.Networks.Test)
      val from = Seed.random
      val to = Seed.random
      Await.ready(horizon.friendbot.create(from.accountId), 1.minute)
      val sourceAccountDetails = Await.result(horizon.account.detail(from.accountId), 10.seconds)

      val transaction = Transaction(
        networkId = Horizon.Networks.Test.id,
        source = from.accountId,
        sequence = sourceAccountDetails.nextSequence,
        operations = List(
          CreateAccount(accountId = to.accountId, startingBalance = Lumen(5).units)
        ),
        maxFee = 100,
      ).sign(from)

      val response = horizon.transact(from.accountId, transaction)

      response must beLike[TransactionResponse] { res =>
        res.operationEvents mustEqual List(
          AccountCreated(
            accountId = to.accountId,
            startingBalance = Lumen(5).units,
            fundingAccountId = from.accountId
          )
        )
        res.feeCharged.units mustEqual 100L
      }.await(0, 10.seconds)
    }

    "be able to transact a payment" >> {
      val horizon = Horizon.async(Horizon.Networks.Test)
      val from = Seed.random
      val to = Seed.random
      Await.ready(Future.sequence(List(
        horizon.friendbot.create(from.accountId),
        horizon.friendbot.create(to.accountId)
      )), 1.minute)
      val sourceAccountDetails = Await.result(horizon.account.detail(from.accountId), 10.seconds)

      val transaction = Transaction(
        networkId = Horizon.Networks.Test.id,
        source = from.accountId,
        sequence = sourceAccountDetails.nextSequence,
        operations = List(
          Pay(recipient = to.address, amount = Lumen(5))
        ),
        maxFee = 100,
      ).sign(from)

      val response = horizon.transact(from.accountId, transaction)

      response must beLike[TransactionResponse] { res =>
        res.operationEvents mustEqual List(
          PaymentMade(
            from = from.accountId,
            to = to.address,
            amount = Lumen(5)
          )
        )
        res.feeCharged.units mustEqual 100L
      }.await(0, 10.seconds)
    }
  }

}
