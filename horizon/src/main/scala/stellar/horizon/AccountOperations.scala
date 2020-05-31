package stellar.horizon

import okhttp3.Response
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats}
import stellar.horizon.json.AccountDetailReader
import stellar.protocol.AccountId

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * Operations related to Horizon endpoints for accounts.
 * @tparam F the effect type.
 */
trait AccountOperations[F[_]] {
  protected val horizon: Horizon[F]

  def accountDetail(accountId: AccountId): F[AccountDetail]
  protected def accountDetailResponse(accountId: AccountId): F[Response] =
    horizon.get(s"accounts/${accountId.encodeToString}")

}

/**
 * Account operations effected by Scala Try.
 * @param horizon how to access the Horizon instance
 */
class AccountOperationsBlockingInterpreter(
  protected val horizon: Horizon[Try]
) extends AccountOperations[Try] {
  implicit val formats: Formats = DefaultFormats + AccountDetailReader

  override def accountDetail(accountId: AccountId): Try[AccountDetail] =
    accountDetailResponse(accountId).map(response => parse(response.body().string()).extract[AccountDetail])
}

/**
 * Account operations effected by Scala Future.
 * @param horizon how to access the Horizon instance
 * @param ec a Future execution context
 */
class AccountOperationsAsyncInterpreter(
  protected val horizon: Horizon[Future]
)(implicit ec: ExecutionContext) extends AccountOperations[Future] {
  implicit val formats: Formats = DefaultFormats + AccountDetailReader

  override def accountDetail(accountId: AccountId): Future[AccountDetail] =
    accountDetailResponse(accountId).map(response => parse(response.body().string()).extract[AccountDetail])
}
