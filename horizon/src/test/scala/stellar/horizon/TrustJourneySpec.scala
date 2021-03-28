package stellar.horizon

import java.util.concurrent.ConcurrentLinkedQueue

import com.typesafe.scalalogging.LazyLogging
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.event.{TrustChangeFailed, TrustChanged, TrustRemoved}
import stellar.event.TrustChangeFailed.IssuerDoesNotExist
import stellar.horizon.ValidationResult.Valid
import stellar.horizon.testing.TestAccountPool
import stellar.protocol.op.TrustAsset
import stellar.protocol.{Seed, Token, Transaction}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.{CollectionHasAsScala, IteratorHasAsScala}

class TrustJourneySpec(implicit ee: ExecutionEnv) extends Specification with LazyLogging {

  private val horizon = Horizon.async(Horizon.Networks.Test)
  private lazy val testAccountPool = Await.result(TestAccountPool.create(15), 1.minute)
  private val cleanUpOperations = new ConcurrentLinkedQueue[() => Future[TransactionResponse]]()

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

    "fail when the lumen balance minus the lumen selling liabilities is insufficient to cover a new trustline entry" >>
      pending("trading")

    "otherwise succeed" >> {
      val (trustee, trustor) = testAccountPool.borrowPair
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

      clearTrust(trustor, asset)

      response must beLike[TransactionResponse] { res =>
        res.accepted must beTrue
        res.operationEvents mustEqual List(
          TrustChanged(
            source = trustor.address,
            token = asset,
            limit = 100_000_000L
          )
        )
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }.await(0, 10.seconds)
    }
  }

  "changing the trust level for an asset" should {
    "fail when the new limit is insufficient to cover balance plus buying liabilities" >> pending("trading")
  }

  "removing trust of an asset" should {
    "succeed when trust exists" >> {
      val (trustee, trustor) = testAccountPool.borrowPair
      val asset = Token("KOUGIRA", trustee.accountId)
      val response = for {
        fromAccountDetails <- horizon.account.detail(trustor.accountId)
        // TODO - a simplified version? `horizon.transact(source, operation*, [fee]).sign(key)`
        _ <- horizon.transact(Transaction(
          networkId = horizon.networkId,
          source = trustor.accountId,
          sequence = fromAccountDetails.nextSequence,
          operations = List(
            TrustAsset(asset, 100_000_000L)
          ),
          maxFee = 100
        ).sign(trustor))
        response <- horizon.transact(Transaction(
          networkId = horizon.networkId,
          source = trustor.accountId,
          sequence = fromAccountDetails.nextSequence + 1,
          operations = List(
            TrustAsset.removeTrust(asset)
          ),
          maxFee = 100
        ).sign(trustor))
      } yield response
      response must beLike[TransactionResponse] { res =>
        res.accepted must beTrue
        res.operationEvents mustEqual List(
          TrustRemoved(
            source = trustor.address,
            token = asset
          )
        )
        res.feeCharged mustEqual 100L
        res.validationResult mustEqual Valid
      }.await(0, 30.seconds)
    }

    "fail when trust does not exist" >> pending("todo")
  }

  // Close the accounts and return their funds back to friendbot
  step { logger.info("Ensuring all tests are complete before closing pool.") }
  step { Await.result(Future.sequence(cleanUpOperations.iterator().asScala.map(_.apply)), 1.minute) }
  step { Await.result(testAccountPool.close(), 10.minute) }

  def clearTrust(seed: Seed, asset: Token): Unit = cleanUpOperations.add(() =>
    for {
      fromAccountDetails <- horizon.account.detail(seed.accountId)
      response <- horizon.transact(Transaction(
        networkId = horizon.networkId,
        source = seed.accountId,
        sequence = fromAccountDetails.nextSequence,
        operations = List(TrustAsset.removeTrust(asset)),
        maxFee = 100
      ).sign(seed))
    } yield response
  )

}
