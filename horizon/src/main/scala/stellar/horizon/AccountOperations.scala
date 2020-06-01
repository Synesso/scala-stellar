package stellar.horizon

import okhttp3.{HttpUrl, Request}
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats}
import stellar.horizon.io.HttpExchange
import stellar.horizon.json.AccountDetailReader
import stellar.protocol.AccountId

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object AccountOperations {
  object Implicits {
    implicit val formats: Formats = DefaultFormats + AccountDetailReader
  }

  def accountDetailRequest(horizonBaseUrl: HttpUrl, accountId: AccountId): Request =
    new Request.Builder()
    .url(
      horizonBaseUrl
        .newBuilder()
        .addPathSegment(s"accounts")
        .addPathSegment(accountId.encodeToString)
        .build())
    .build()
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
 */
class AccountOperationsSyncInterpreter(
  horizonBaseUrl: HttpUrl,
  httpExchange: HttpExchange[Try]
) extends AccountOperations[Try] {

  import AccountOperations.Implicits._

  override def accountDetail(accountId: AccountId): Try[AccountDetail] = {
    val request = AccountOperations.accountDetailRequest(horizonBaseUrl, accountId)
    for {
      response <- httpExchange.invoke(request)
      result <- Try(parse(response.body().string()).extract[AccountDetail])
    } yield result
  }

}

/**
 * Account operations effected by Scala Future.
 */
class AccountOperationsAsyncInterpreter(
  horizonBaseUrl: HttpUrl,
  httpExchange: HttpExchange[Future]
)(implicit ec: ExecutionContext) extends AccountOperations[Future] {

  import AccountOperations.Implicits._

  override def accountDetail(accountId: AccountId): Future[AccountDetail] = {
    val request = AccountOperations.accountDetailRequest(horizonBaseUrl, accountId)
    for {
      response <- httpExchange.invoke(request)
      result <- Future(parse(response.body().string()).extract[AccountDetail])
    } yield result
  }
}
