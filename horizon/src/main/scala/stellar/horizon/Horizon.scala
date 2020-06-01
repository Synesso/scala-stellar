package stellar.horizon

import okhttp3.{HttpUrl, OkHttpClient}
import stellar.horizon.io.{HttpExchange, HttpExchangeAsyncInterpreter, HttpExchangeSyncInterpreter}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


object Horizon {
  def sync(
    baseUrl: HttpUrl,
    httpClient: OkHttpClient = new OkHttpClient(),
    createHttpExchange: OkHttpClient => HttpExchange[Try] = new HttpExchangeSyncInterpreter(_)
  ): Horizon[Try] = {
    val httpExchange = createHttpExchange(httpClient)

    new Horizon[Try] {
      override def accounts: AccountOperations[Try] = new AccountOperationsSyncInterpreter(baseUrl, httpExchange)
    }
  }

  def async(
    baseUrl: HttpUrl,
    httpClient: OkHttpClient = new OkHttpClient(),
    createHttpExchange: (OkHttpClient, ExecutionContext) => HttpExchange[Future] = { (httpClient, ec) =>
      new HttpExchangeAsyncInterpreter(httpClient)(ec)
    }
  )(implicit ec: ExecutionContext): Horizon[Future] = {
    val httpExchange = createHttpExchange(httpClient, ec)

    new Horizon[Future] {
      override def accounts: AccountOperations[Future] = new AccountOperationsAsyncInterpreter(baseUrl, httpExchange)
    }
  }
}

sealed trait Horizon[F[_]] {
  def accounts: AccountOperations[F]
}
