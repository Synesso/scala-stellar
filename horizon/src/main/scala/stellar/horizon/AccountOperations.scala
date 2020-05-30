package stellar.horizon

import okhttp3.{HttpUrl, Request}
import org.json4s.{DefaultFormats, Formats}
import org.json4s.native.JsonMethods.parse
import stellar.horizon.io.{HttpExchange, HttpExchangeAsync, HttpExchangeSync}
import stellar.protocol.AccountId

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object AccountOperations {

  def blocking(baseUrl: HttpUrl): AccountOperations[Try] =
    new AccountOperationsBlockingInterpreter(baseUrl, HttpExchangeSync)

  def async(baseUrl: HttpUrl)(implicit ec: ExecutionContext): AccountOperations[Future] =
    new AccountOperationsAsyncInterpreter(baseUrl, HttpExchangeAsync)
}

/**
 * Operations related to Horizon endpoints for accounts.
 * @tparam F the effect type.
 */
trait AccountOperations[F[_]] {
  def accountDetail(accountId: AccountId): F[AccountDetail]
}

/**
 * Account operations effected by Scala Try.
 * @param baseUrl the base url for the Horizon instance
 * @param httpExchange the HTTP exchange handler
 */
class AccountOperationsBlockingInterpreter(
  baseUrl: HttpUrl,
  httpExchange: HttpExchange[Try] // TODO (jem) - It feels like the base url and exchange should be grouped in a Horizon class.
) extends AccountOperations[Try] {
  implicit val formats: Formats = DefaultFormats + AccountDetailReader

  override def accountDetail(accountId: AccountId): Try[AccountDetail] = {
    val request = new Request.Builder() // TODO (jem) - this can defo be in a Horizon class. Fo sho.
      .url(baseUrl.newBuilder()
        .addPathSegment("accounts")
        .addPathSegment(accountId.encodeToString)
        .build)
      .build

    httpExchange.invoke(request)
        .map(response => parse(response.body().string()).extract[AccountDetail])
  }
}

/**
 * Account operations effected by Scala Future.
 * @param baseUrl the base url for the Horizon instance
 * @param httpExchange the HTTP exchange handler
 * @param ec a Future execution context
 */
class AccountOperationsAsyncInterpreter(
  baseUrl: HttpUrl,
  httpExchange: HttpExchange[Future]
)(implicit ec: ExecutionContext) extends AccountOperations[Future] {
  implicit val formats: Formats = DefaultFormats + AccountDetailReader

  override def accountDetail(accountId: AccountId): Future[AccountDetail] = {
    val request = new Request.Builder()
      .url(baseUrl.newBuilder()
        .addPathSegment("accounts")
        .addPathSegment(accountId.encodeToString)
        .build)
      .build

    httpExchange.invoke(request)
        .map(response => parse(response.body().string()).extract[AccountDetail])
  }
}



/*

object Program {
  def resetPassword(accountId: AccountId, passwordInput: PasswordInput)
    (implicit ops: AllOperation): Future[AccountDetail] = {
    import ops._

    for {
      accountDetail <- accountOperations.accountDetail(accountId)
      validatedPasswordInput <- passwordResetOperation.validate(passwordInput)
      _ <- passwordResetOperation.reset(accountDetail, validatedPasswordInput)
    }
  }
}
*/
