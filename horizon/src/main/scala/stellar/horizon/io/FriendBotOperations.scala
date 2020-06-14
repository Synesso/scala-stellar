package stellar.horizon.io

import okhttp3.{HttpUrl, Request}
import stellar.horizon.TransactionResponse
import stellar.horizon.io.HttpOperations.NotFound
import stellar.protocol.AccountId

import scala.util.Try

object FriendBotOperations {
  def createAccountRequest(friendBotUrl: HttpUrl, accountId: AccountId): Request =
    new Request.Builder()
      .url(
        friendBotUrl
          .newBuilder()
          .addQueryParameter("addr", accountId.encodeToString)
          .build())
      .build()
}

/**
 * The one and only operation related to Horizon instances that have FriendBot enabled.
 * @tparam F the effect type.
 */
trait FriendBotOperations[F[_]] {
  /**
   * Creates and funds the account specified by the provided address, but only if FriendBot is enabled on this
   * Horizon instance.
   * @param accountId the account to be created and funded
   * @return the result of the transaction that funded the account
   */
  def create(accountId: AccountId): F[TransactionResponse]
}

class FriendBotOperationsSyncInterpreter(
  horizonBaseUrl: HttpUrl,
  httpExchange: HttpOperations[Try]
) extends FriendBotOperations[Try] {

  private val meta = new MetaOperationsSyncInterpreter(horizonBaseUrl, httpExchange)

  override def create(accountId: AccountId): Try[TransactionResponse] = for {
    friendBotUrl <- meta.state.map(_.friendbotUrl.getOrElse(throw NotFound()))
    request = FriendBotOperations.createAccountRequest(friendBotUrl, accountId)
    response <- httpExchange.invoke(request)
    result <- httpExchange.handle(response, Try(TransactionOperations.responseToTransactionResponse(response)))
  } yield result
}
