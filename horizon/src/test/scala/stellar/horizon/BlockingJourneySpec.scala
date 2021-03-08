package stellar.horizon

import com.typesafe.scalalogging.LazyLogging
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.Matchers
import org.specs2.mutable.Specification
import stellar.event.AccountCreated
import stellar.horizon.io.HttpOperations.NotFound
import stellar.horizon.testing.TestAccountPool
import stellar.protocol.op.CreateAccount
import stellar.protocol.{AccountId, Address, Lumen, Seed, Transaction}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Try

/**
 * Top level tests that demonstrate how to use the blocking endpoints.
 */
class BlockingJourneySpec(implicit ee: ExecutionEnv) extends Specification with Matchers with LazyLogging {

  private val FriendBotAddress = Address("GAIH3ULLFQ4DGSECF2AR555KZ4KNDGEKN4AFI4SU2M7B43MGK3QJZNSR")
  private lazy val testAccountPool = Await.result(TestAccountPool.create(1), 1.minute)

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
        res.feeCharged must beGreaterThanOrEqualTo(100L)
        res.accepted must beTrue
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

    "be able to create a new account" >> {
      val horizon = Horizon.sync(Horizon.Networks.Test)
      val from = testAccountPool.borrow
      val to = Seed.random

      val response = for {
        sourceAccountDetails <- horizon.account.detail(from.accountId)
        transaction = Transaction(
          networkId = Horizon.Networks.Test.id,
          source = from.accountId,
          sequence = sourceAccountDetails.nextSequence,
          operations = List(
            CreateAccount(accountId = to.accountId, startingBalance = Lumen(5).units)
          ),
          maxFee = 100,
        ).sign(from)
        response <- horizon.transact(transaction)
      } yield response

      response must beASuccessfulTry[TransactionResponse].like { res =>
        res.operationEvents mustEqual List(
          AccountCreated(
            accountId = to.accountId,
            startingBalance = Lumen(5).units,
            source = from.address
          )
        )
        res.feeCharged mustEqual 100L
        res.accepted must beTrue
      }
    }

    // Close the accounts and return their funds back to friendbot
    step { logger.info("Ensuring all tests are complete before closing pool.") }
    step { Await.result(testAccountPool.close(), 10.minute) }
  }

}
