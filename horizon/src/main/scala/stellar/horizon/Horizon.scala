package stellar.horizon

import okhttp3.{HttpUrl, OkHttpClient}
import stellar.horizon.io._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


object Horizon {

  object Endpoints {
    val Main = HttpUrl.parse("https://horizon.stellar.org/")
    val Test = HttpUrl.parse("https://horizon-testnet.stellar.org/")
  }

  def sync(
    baseUrl: HttpUrl = Endpoints.Main,
    httpClient: OkHttpClient = new OkHttpClient(),
    createHttpExchange: OkHttpClient => HttpOperations[Try] = { httpClient =>
      new HttpOperationsSyncInterpreter(
        exchange = HttpOperationsSyncInterpreter.exchange(httpClient, _)
      )
    }
  ): Horizon[Try] = {
    val httpExchange = createHttpExchange(httpClient)

    new Horizon[Try] {
      override def account: AccountOperations[Try] = new AccountOperationsSyncInterpreter(baseUrl, httpExchange)
      override def friendbot: FriendBotOperations[Try] = new FriendBotOperationsSyncInterpreter(baseUrl, httpExchange)
      override def meta: MetaOperations[Try] = new MetaOperationsSyncInterpreter(baseUrl, httpExchange)
    }
  }

  def async(
    baseUrl: HttpUrl = Endpoints.Main,
    httpClient: OkHttpClient = new OkHttpClient(),
    createHttpExchange: (OkHttpClient, ExecutionContext) => HttpOperations[Future] = { (httpClient, ec) =>
      new HttpOperationsAsyncInterpreter(
        exchange = HttpOperationsAsyncInterpreter.exchange(httpClient, _)(ec)
      )
    }
  )(implicit ec: ExecutionContext): Horizon[Future] = {
    val httpExchange = createHttpExchange(httpClient, ec)

    new Horizon[Future] {
      override def account: AccountOperations[Future] = new AccountOperationsAsyncInterpreter(baseUrl, httpExchange)
      override def friendbot: FriendBotOperations[Future] = new FriendBotOperationsAsyncInterpreter(baseUrl, httpExchange)
      override def meta: MetaOperations[Future] = new MetaOperationsAsyncInterpreter(baseUrl, httpExchange)
    }
  }
}

sealed trait Horizon[F[_]] {
  def account: AccountOperations[F]
  def friendbot: FriendBotOperations[F]
  def meta: MetaOperations[F]
}
