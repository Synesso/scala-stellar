package stellar.horizon

import okhttp3.{HttpUrl, OkHttpClient}
import stellar.horizon.io._
import stellar.protocol.op.Operation
import stellar.protocol.{Amount, Lumen, NetworkId, Seed, Transaction}

import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


object Horizon {

  val MinFeePerOperationInStroops = 100

  private val DefaultOkHttpClient = new OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(10, TimeUnit.SECONDS)
    .readTimeout(1, TimeUnit.MINUTES)
    .build()

  object Networks {
    val Main: Network = Network(
      // TODO - these network ids are not specific to Horizon
      NetworkId("Public Global Stellar Network ; September 2015"),
      HttpUrl.parse("https://horizon.stellar.org/")
    )
    val Test: Network = Network(
      NetworkId("Test SDF Network ; September 2015"),
      HttpUrl.parse("https://horizon-testnet.stellar.org/")
    )
  }

  def sync(
    network: Network = Networks.Main,
    httpClient: OkHttpClient = DefaultOkHttpClient,
    createHttpExchange: OkHttpClient => HttpOperations[Try] = { httpClient =>
      new HttpOperationsSyncInterpreter(
        exchange = HttpOperationsSyncInterpreter.exchange(httpClient, _)
      )
    }
  ): Horizon[Try] = {
    val httpExchange = createHttpExchange(httpClient)

    new Horizon[Try] {
      override val networkId: NetworkId = network.id

      override def account: AccountOperations[Try] = new AccountOperationsSyncInterpreter(network.url, httpExchange)

      override def friendbot: FriendBotOperations[Try] = new FriendBotOperationsSyncInterpreter(network.url, httpExchange)

      override def meta: MetaOperations[Try] = new MetaOperationsSyncInterpreter(network.url, httpExchange)

      override def transact(transaction: Transaction): Try[TransactionResponse] =
        new TransactionOperationsSyncInterpreter(network.url, httpExchange).transact(transaction)

      override def transact(seed: Seed, operations: List[Operation]): Try[TransactionResponse] = for {
        sequence <- account.detail(seed.accountId).map(_.nextSequence)
        response <- transact(simpleTransaction(networkId, seed, operations, sequence))
      } yield response
    }
  }

  def async(
    network: Network = Networks.Main,
    httpClient: OkHttpClient = DefaultOkHttpClient,
    createHttpExchange: (OkHttpClient, ExecutionContext) => HttpOperations[Future] = { (httpClient, ec) =>
      new HttpOperationsAsyncInterpreter(
        exchange = HttpOperationsAsyncInterpreter.exchange(httpClient, _)(ec)
      )
    }
  )(implicit ec: ExecutionContext): Horizon[Future] = {
    val httpExchange = createHttpExchange(httpClient, ec)

    new Horizon[Future] {
      override val networkId: NetworkId = network.id

      override def account: AccountOperations[Future] = new AccountOperationsAsyncInterpreter(network.url, httpExchange)

      override def friendbot: FriendBotOperations[Future] = new FriendBotOperationsAsyncInterpreter(network.url, httpExchange)

      override def meta: MetaOperations[Future] = new MetaOperationsAsyncInterpreter(network.url, httpExchange)

      override def transact(transaction: Transaction): Future[TransactionResponse] =
        new TransactionOperationsAsyncInterpreter(network.url, httpExchange).transact(transaction)

      override def transact(seed: Seed, operations: List[Operation]): Future[TransactionResponse] = for {
        sequence <- account.detail(seed.accountId).map(_.nextSequence)
        response <- transact(simpleTransaction(networkId, seed, operations, sequence))
      } yield response
    }
  }

  private def simpleTransaction(networkId: NetworkId, seed: Seed, operations: List[Operation], sequence: Long) = {
    Transaction(
      networkId = networkId,
      source = seed.accountId,
      sequence = sequence,
      operations = operations,
      maxFee = MinFeePerOperationInStroops * operations.size
    ).sign(seed)
  }
}

sealed trait Horizon[F[_]] {
  val networkId: NetworkId

  /**
   * Access operations related to accounts.
   */
  def account: AccountOperations[F]

  /**
   * Access account creation operations on test networks.
   */
  def friendbot: FriendBotOperations[F]

  /**
   * Access operations pertaining to the network itself.
   */
  def meta: MetaOperations[F]

  /**
   * Issue the given transaction to the network.
   */
  def transact(transaction: Transaction): F[TransactionResponse]

  /**
   * A convenience method for transacting with a single signer.
   */
  def transact(seed: Seed, operations: List[Operation]): F[TransactionResponse]
}
