package stellar.horizon

import okhttp3.{HttpUrl, Request, Response}
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats}
import stellar.horizon.io.HttpOperations
import stellar.horizon.io.HttpOperations.NotFound
import stellar.horizon.json.AccountDetailReader
import stellar.protocol.AccountId

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

object AccountOperations {
  def accountDetailRequest(horizonBaseUrl: HttpUrl, accountId: AccountId): Request =
    new Request.Builder()
    .url(
      horizonBaseUrl
        .newBuilder()
        .addPathSegment(s"accounts")
        .addPathSegment(accountId.encodeToString)
        .build())
    .build()

  def responseToAccountDetails(response: Response): AccountDetail = {
    implicit val formats: Formats = DefaultFormats + AccountDetailReader
    parse(response.body().string()).extract[AccountDetail]
  }
}

/**
 * Operations related to Horizon endpoints for accounts.
 * @tparam F the effect type.
 */
trait AccountOperations[F[_]] {
  def detail(accountId: AccountId): F[AccountDetail]
}

/**
 * Account operations effected by Scala Try.
 */
class AccountOperationsSyncInterpreter(
  horizonBaseUrl: HttpUrl,
  httpExchange: HttpOperations[Try]
) extends AccountOperations[Try] {

  override def detail(accountId: AccountId): Try[AccountDetail] = {
    val request = AccountOperations.accountDetailRequest(horizonBaseUrl, accountId)
    for {
      response <- httpExchange.invoke(request)
      result <- response.code() match {
        case 200 => Try(AccountOperations.responseToAccountDetails(response))
        case 404 => Failure(NotFound(s"account.detail(${accountId.encodeToString})"))
      }
    } yield result
  }

}

/**
 * Account operations effected by Scala Future.
 */
class AccountOperationsAsyncInterpreter(
  horizonBaseUrl: HttpUrl,
  httpExchange: HttpOperations[Future]
)(implicit ec: ExecutionContext) extends AccountOperations[Future] {

  override def detail(accountId: AccountId): Future[AccountDetail] = {
    val request = AccountOperations.accountDetailRequest(horizonBaseUrl, accountId)
    for {
      response <- httpExchange.invoke(request)
      result <- response.code() match {
        case 200 => Future(AccountOperations.responseToAccountDetails(response))
        case 404 => Future { throw NotFound(s"account.detail(${accountId.encodeToString})") }
      }
    } yield result
  }
}
