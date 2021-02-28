package stellar.horizon.io

import okhttp3.{HttpUrl, MultipartBody, Request, Response}
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats}
import stellar.horizon.TransactionResponse
import stellar.horizon.json.TransactionResponseReader
import stellar.protocol.{AccountId, Transaction}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object TransactionOperations {

  def postTransactionRequest(horizonBaseUrl: HttpUrl, transaction: Transaction): Request =
    new Request.Builder()
      .url(horizonBaseUrl.newBuilder()
        .addPathSegment("transactions")
        .build)
      .post(new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("tx", transaction.encodeSigned.base64())
        .build)
      .build

  def responseToTransactionResponse(response: Response): TransactionResponse = {
    implicit val formats: Formats = DefaultFormats + TransactionResponseReader
    parse(response.body().string()).extract[TransactionResponse]
  }
}

/**
 * Operations related to Horizon endpoints for transacting.
 *
 * @tparam F the effect type.
 */
trait TransactionOperations[F[_]] {

  /**
   * Performs the specified payment.
   *
   * param sender    the issuer of the funds
   * param recipient the receiver of the funds
   * param amount    the amount to be sent
   * return the result of the transaction attempted
   * def pay(
   * sender: Address,
   * recipient: Address,
   * amount: Amount
   * ): F[TransactionResponse]
   */

  def transact(
    transaction: Transaction
  ): F[TransactionResponse]

  /**
   * Creates a new account funded by the creator of the transaction.
   *
   * param destination     the account to be created
   * param startingBalance the amount to be sent to create the account
   * param fundingAccount
   * return the result of the transaction attempted
   * def createAccount(
   * destination: AccountId,
   * startingBalance: Long
   * // fundingAccount: Option[AccountId] // TODO - can this be bundled with a signing strategy
   * ): F[TransactionResponse]
   */
}

/**
 * Transaction operations effected by Scala Try.
 */
class TransactionOperationsSyncInterpreter(
  horizonBaseUrl: HttpUrl,
  httpExchange: HttpOperations[Try]
) extends TransactionOperations[Try] {

  override def transact(transaction: Transaction): Try[TransactionResponse] = {
    val request = TransactionOperations.postTransactionRequest(horizonBaseUrl, transaction)
    for {
      response <- httpExchange.invoke(request)
      result <- httpExchange.handle(response, Try(TransactionOperations.responseToTransactionResponse(response)))
    } yield result
  }

}

/**
 * Transaction operations effected by Scala Future.
 */
class TransactionOperationsAsyncInterpreter(
  horizonBaseUrl: HttpUrl,
  httpExchange: HttpOperations[Future]
)(implicit ec: ExecutionContext) extends TransactionOperations[Future] {

  override def transact(transaction: Transaction): Future[TransactionResponse] = {
    val request = TransactionOperations.postTransactionRequest(horizonBaseUrl, transaction)
    for {
      response <- httpExchange.invoke(request)
      result <- httpExchange.handle(response, Future(TransactionOperations.responseToTransactionResponse(response)))
    } yield result
  }


  /**
   * Performs the specified payment.
   *
   * param sender    the issuer of the funds
   * param recipient the receiver of the funds
   * param amount    the amount to be sent
   * return the result of the transaction attempted
   */
  /*
    override def pay(sender: Address, recipient: Address, amount: Amount): Future[TransactionResponse] = {
      for {
        account <- accountOperations.detail(sender.accountId)
        transaction = Transaction(
          network = networkId,
          source = sender.accountId,
          sequence = account.sequence + 1,
          operations = NonEmptyList.of(Payment(Some(sender), recipient, amount)),
          // TODO (jem) - set max fee per operation on the TransactionOperations instance.
          maxFee = 100,
          keys = List(signer)
        )
        request = TransactionOperations.postTransactionRequest(horizonBaseUrl, transaction)
        response <- httpExchange.invoke(request)
        result <- httpExchange.handle(response, Future(TransactionOperations.responseToTransactionResponse(response)))
      } yield result
    }
  */


  /**
   * Creates a new account funded by the creator of the transaction.
   *
   * param destination     the account to be created
   * param startingBalance the amount to be sent to create the account
   * return the result of the transaction attempted
   * def createAccount(
   * destination: AccountId,
   * startingBalance: Long
   * ): Future[TransactionResponse] = {
   * val source = signer
   * for {
   * account <- accountOperations.detail(sender.accountId)
   * transaction = Transaction(
   * network = networkId,
   * source = sender.accountId,
   * sequence = account.sequence + 1,
   * operations = NonEmptyList.of(Payment(Some(sender), recipient, amount)),
   * // TODO (jem) - set max fee per operation on the TransactionOperations instance.
   * maxFee = 100,
   * keys = List(signer)
   * )
   * request = TransactionOperations.postTransactionRequest(horizonBaseUrl, transaction)
   * response <- httpExchange.invoke(request)
   * result <- httpExchange.handle(response, Future(TransactionOperations.responseToTransactionResponse(response)))
   * } yield result
   */


}
