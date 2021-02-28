package stellar.horizon

import org.specs2.matcher.Matchers
import org.specs2.mutable.Specification
import stellar.event.AccountCreated
import stellar.horizon.io.HttpOperations.NotFound
import stellar.protocol.{AccountId, Address, Lumen, Seed}

import scala.util.Try

/**
 * Top level tests that demonstrate how to use the blocking endpoints.
 */
class BlockingJourneySpec extends Specification with Matchers {

  private val FriendBotAddress = Address("GAIH3ULLFQ4DGSECF2AR555KZ4KNDGEKN4AFI4SU2M7B43MGK3QJZNSR")

  "client software" should {

    "be able to fetch horizon instance capabilities" >> {
      val horizon = Horizon.sync()
      val state: Try[HorizonState] = horizon.meta.state
      state must beSuccessfulTry[HorizonState].like { s =>
        s.friendbotUrl must beNone
        s.networkPassphrase mustEqual "Public Global Stellar Network ; September 2015"
      }
    }

    "be able to create a new account from a faucet (friendbot), if one is available" >> {
      val horizon = Horizon.sync(Horizon.Networks.Test)
      val accountId = Seed.random.accountId
      val response = horizon.friendbot.create(accountId)
      response must beSuccessfulTry[TransactionResponse].like { res =>
        res.operationEvents mustEqual List(
          AccountCreated(
            accountId = accountId,
            startingBalance = Lumen(10_000).units,
            source = FriendBotAddress
          )
        )
        res.feeCharged.units must beGreaterThanOrEqualTo(100L)
      }
    }

    "fail to create a new account from a faucet (friendbot), if none is available" >> {
      val horizon = Horizon.sync(Horizon.Networks.Main)
      val accountId = Seed.random.accountId
      val response = horizon.friendbot.create(accountId)
      response must beFailedTry[TransactionResponse].like { _ must beAnInstanceOf[NotFound] }
    }

    "be able to fetch account details" >> {
      val horizon = Horizon.sync()
      val accountId = AccountId("GBRAZP7U3SPHZ2FWOJLHPBO3XABZLKHNF6V5PUIJEEK6JEBKGXWD2IIE")
      val accountDetail: Try[AccountDetail] = horizon.account.detail(accountId)
      accountDetail must beSuccessfulTry.like(_.id mustEqual accountId)
    }
  }

}
