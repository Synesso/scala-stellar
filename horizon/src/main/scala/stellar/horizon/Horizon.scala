package stellar.horizon

import okhttp3.HttpUrl
import stellar.horizon.io.{HttpExchange, HttpExchangeAsync, HttpExchangeSync}
import stellar.protocol.AccountId

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

abstract class Horizon[F[_]](
  protected val baseUrl: HttpUrl,
  protected val httpExchange: HttpExchange[F])
{

  val accountOps: AccountOperations[F]

  def accountDetail(accountId: AccountId): F[AccountDetail]
    = accountOps.accountDetail(accountId)

}

class BlockingHorizon(baseUrl: HttpUrl) extends Horizon[Try](baseUrl, HttpExchangeSync) {
  override val accountOps: AccountOperations[Try] = new AccountOperationsBlockingInterpreter(baseUrl, httpExchange)
}

//HttpUrl.parse("https://horizon-testnet.stellar.org/")
class HorizonAsync(baseUrl: HttpUrl)(implicit ec: ExecutionContext) extends Horizon[Future](baseUrl, HttpExchangeAsync) {
  override val accountOps: AccountOperations[Future] = new AccountOperationsAsyncInterpreter(baseUrl, httpExchange)
}


// TODO (jem) - Next up, let's get test coverage around the different kinds of horizons, using the FakeHttpExchange