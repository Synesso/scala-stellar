package stellar.horizon

import okhttp3.{HttpUrl, OkHttpClient}
import stellar.horizon.io._
import stellar.protocol.{Key, NetworkId, SigningKey}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


object Horizon {

  object Networks {
    val Main = Network(
      NetworkId("Public Global Stellar Network ; September 2015"),
      HttpUrl.parse("https://horizon.stellar.org/")
    )
    val Test = Network(
      NetworkId("Test SDF Network ; September 2015"),
      HttpUrl.parse("https://horizon-testnet.stellar.org/")
    )
  }

  def sync(
    network: Network = Networks.Main,
    httpClient: OkHttpClient = new OkHttpClient(),
    createHttpExchange: OkHttpClient => HttpOperations[Try] = { httpClient =>
      new HttpOperationsSyncInterpreter(
        exchange = HttpOperationsSyncInterpreter.exchange(httpClient, _)
      )
    }
  ): Horizon[Try] = {
    val httpExchange = createHttpExchange(httpClient)

    new Horizon[Try] {
      override def account: AccountOperations[Try] = new AccountOperationsSyncInterpreter(network.url, httpExchange)
      override def friendbot: FriendBotOperations[Try] = new FriendBotOperationsSyncInterpreter(network.url, httpExchange)
      override def meta: MetaOperations[Try] = new MetaOperationsSyncInterpreter(network.url, httpExchange)
      override def transact(signer: SigningKey): TransactionOperations[Try] =
        new TransactionOperationsSyncInterpreter(network.url, httpExchange, signer, network.id, account)
    }
  }

  def async(
    network: Network = Networks.Main,
    httpClient: OkHttpClient = new OkHttpClient(),
    createHttpExchange: (OkHttpClient, ExecutionContext) => HttpOperations[Future] = { (httpClient, ec) =>
      new HttpOperationsAsyncInterpreter(
        exchange = HttpOperationsAsyncInterpreter.exchange(httpClient, _)(ec)
      )
    }
  )(implicit ec: ExecutionContext): Horizon[Future] = {
    val httpExchange = createHttpExchange(httpClient, ec)

    new Horizon[Future] {
      override def account: AccountOperations[Future] = new AccountOperationsAsyncInterpreter(network.url, httpExchange)
      override def friendbot: FriendBotOperations[Future] = new FriendBotOperationsAsyncInterpreter(network.url, httpExchange)
      override def meta: MetaOperations[Future] = new MetaOperationsAsyncInterpreter(network.url, httpExchange)
      override def transact(signer: SigningKey): TransactionOperations[Future] =
        new TransactionOperationsAsyncInterpreter(network.url, httpExchange, signer, network.id, account)
    }
  }
}

sealed trait Horizon[F[_]] {
  def account: AccountOperations[F]
  def friendbot: FriendBotOperations[F]
  def meta: MetaOperations[F]
  def transact(signer: SigningKey): TransactionOperations[F]
}
