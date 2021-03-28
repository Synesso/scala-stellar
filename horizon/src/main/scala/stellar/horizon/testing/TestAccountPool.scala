package stellar.horizon.testing

import java.util.concurrent.ConcurrentLinkedQueue

import com.typesafe.scalalogging.LazyLogging
import stellar.horizon.Horizon
import stellar.protocol.op.{CreateAccount, MergeAccount}
import stellar.protocol.{Address, Lumen, Seed, Token, Transaction}

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.IteratorHasAsScala

/**
 * Encapsulates the efficient funding of multiple test accounts and their closing.
 */
class TestAccountPool(
  private val seeds: List[Seed],
  private val friendbotAddress: Address
) extends LazyLogging {

  require(seeds.nonEmpty && seeds.size <= 100,
    s"Can only create between 1 and 100 test accounts (provided $seeds.size)")

  private val free = new ConcurrentLinkedQueue[Seed]()
  private val borrowed = new ConcurrentLinkedQueue[Seed]()
  seeds.foreach(free.add)

  /** Get a unique seed and ensure that it is merged on pool close */
  def borrow: Seed = {
    val s = free.remove()
    borrowed.add(s)
    s
  }

  /** Get a unique seed and let the pool forget about it. It will not be merged on close. */
  def take: Seed = free.remove()

  /** Borrow twice, for a sender and recipient */
  def borrowPair: (Seed, Seed) = (borrow, borrow)

  def size: Int = free.size() + borrowed.size()

  def close()(implicit ec: ExecutionContext): Future[Unit] = {
    if (free.isEmpty && borrowed.isEmpty) Future(())
    else {
      val horizon = Horizon.async(Horizon.Networks.Test)
      val seeds = List.from(free.iterator().asScala) ++ List.from(borrowed.iterator().asScala)
      val closeBatches = seeds.grouped(20)
      Future.sequence(closeBatches.zipWithIndex.map { case (batch, i) =>
        for {
          _ <- Future { Thread.sleep(i * 50L) } // To avoid http outbound starvation on CI servers
          _ = logger.info(s"Closing ${batch.size} test accounts.")
          sourceAccountResponse <- horizon.account.detail(batch.head.accountId)
          mergeAllResponse <- horizon.transact(Transaction(
            networkId = horizon.networkId,
            source = batch.head.accountId,
            sequence = sourceAccountResponse.nextSequence,
            operations = batch.map(seed =>
              MergeAccount(destination = friendbotAddress, source = Some(seed.address))
            ),
            maxFee = batch.size * 100
          ).sign(batch: _*))
        } yield mergeAllResponse
      }).map(_ => ())
    }
  }
}

object TestAccountPool extends LazyLogging {

  def create(quantity: Int)(implicit ec: ExecutionContext): Future[TestAccountPool] = {
    require(quantity >= 1 && quantity <= 100, s"Can only create between 1 and 100 test accounts (provided $quantity)")
    val horizon = Horizon.async(Horizon.Networks.Test)
    val first :: others = LazyList.continually(Seed.random).take(quantity).toList
    val fee = 100 * others.size
    for {
      friendbotCreateResponse <- horizon.friendbot.create(first.accountId)
      _ = logger.info(s"Creating $quantity test accounts.")
      sourceAccountResponse <- horizon.account.detail(first.accountId)
      startingBalance = sourceAccountResponse.balance(Lumen).map(_.units - fee).map(_ / quantity).get
      _ <- if (others.nonEmpty) horizon.transact(Transaction(
          networkId = horizon.networkId,
          source = first.accountId,
          sequence = sourceAccountResponse.nextSequence,
          operations = others.map(seed => CreateAccount(seed.accountId, startingBalance)),
          maxFee = fee
        ).sign(first)
      ) else Future.unit
      pool = new TestAccountPool(first :: others, friendbotCreateResponse.operationEvents.head.source)
    } yield pool
  }
}