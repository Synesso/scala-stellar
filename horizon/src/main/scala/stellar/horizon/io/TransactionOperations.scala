package stellar.horizon.io

import cats.data.NonEmptyList
import okhttp3.{HttpUrl, MultipartBody, Request, Response}
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats}
import stellar.horizon.TransactionResponse
import stellar.horizon.json.TransactionResponseReader
import stellar.protocol.op.Payment
import stellar.protocol.{AccountId, Address, Amount, Key, NetworkId, SigningKey, Transaction}

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
        .addFormDataPart("tx", transaction.encodeXdr)
        .build)
      .build

  def responseToTransactionResponse(response: Response): TransactionResponse = {
    implicit val formats: Formats = DefaultFormats + TransactionResponseReader
    parse(response.body().string()).extract[TransactionResponse]
  }
}

/**
 * Operations related to Horizon endpoints for transacting.
 * @tparam F the effect type.
 */
trait TransactionOperations[F[_]] {

  /**
   * Performs the specified payment.
   *
   * @param sender    the issuer of the funds
   * @param recipient the receiver of the funds
   * @param amount    the amount to be sent
   * @return the result of the transaction attempted
   */
  def pay(
    sender: Address,
    recipient: Address,
    amount: Amount
  ): F[TransactionResponse]
}

/**
 * Transaction operations effected by Scala Try.
 */
class TransactionOperationsSyncInterpreter(
  horizonBaseUrl: HttpUrl,
  httpExchange: HttpOperations[Try],
  signer: SigningKey,
  networkId: NetworkId,
  accountOperations: AccountOperations[Try]
) extends TransactionOperations[Try] {
  /**
   * Performs the specified payment.
   *
   * @param sender    the issuer of the funds
   * @param recipient the receiver of the funds
   * @param amount    the amount to be sent
   * @return the result of the transaction attempted
   */
  override def pay(sender: Address, recipient: Address, amount: Amount): Try[TransactionResponse] = ???
}

/**
 * Transaction operations effected by Scala Future.
 */
class TransactionOperationsAsyncInterpreter(
  horizonBaseUrl: HttpUrl,
  httpExchange: HttpOperations[Future],
  signer: SigningKey,
  networkId: NetworkId,
  accountOperations: AccountOperations[Future]
)(implicit ec: ExecutionContext) extends TransactionOperations[Future] {
  /**
   * Performs the specified payment.
   *
   * @param sender    the issuer of the funds
   * @param recipient the receiver of the funds
   * @param amount    the amount to be sent
   * @return the result of the transaction attempted
   */
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

}
