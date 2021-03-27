package stellar.horizon

import com.typesafe.scalalogging.LazyLogging
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.event.TrustChangeFailed
import stellar.event.TrustChangeFailed.IssuerDoesNotExist
import stellar.horizon.ValidationResult.Valid
import stellar.horizon.testing.TestAccountPool
import stellar.protocol.op.TrustAsset
import stellar.protocol.{Seed, Token, Transaction}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class TrustJourneySpec(implicit ee: ExecutionEnv) extends Specification with LazyLogging {

  private val horizon = Horizon.async(Horizon.Networks.Test)
  private lazy val testAccountPool = Await.result(TestAccountPool.create(15), 1.minute)

  "trusting an asset" should {
    "fail when the asset issuer does not exist" >> {
      val trustor = testAccountPool.borrow
      val trustee = Seed.random
      val asset = Token("BTC", trustee.accountId)
      val response = for {
        fromAccountDetails <- horizon.account.detail(trustor.accountId)
        response <- horizon.transact(Transaction(
          networkId = horizon.networkId,
          source = trustor.accountId,
          sequence = fromAccountDetails.nextSequence,
          operations = List(
            TrustAsset(asset, 100_000_000L)
          ),
          maxFee = 100
        ).sign(trustor))
      } yield response

      response must beLike[TransactionResponse] { res =>
        res.accepted must beFalse
        res.operationEvents mustEqual List(
          TrustChangeFailed(
            source = trustor.address,
            failure = IssuerDoesNotExist
          )
        )
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }.await(0, 10.seconds)
    }
  }

  // Close the accounts and return their funds back to friendbot
  step { logger.info("Ensuring all tests are complete before closing pool.") }
  step { Await.result(testAccountPool.close(), 10.minute) }

}
