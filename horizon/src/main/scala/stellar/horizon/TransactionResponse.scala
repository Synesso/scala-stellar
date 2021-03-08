package stellar.horizon

import okio.ByteString
import org.stellar.xdr.OperationType.{ACCOUNT_MERGE, CREATE_ACCOUNT, PAYMENT}
import org.stellar.xdr._
import stellar.event.{CreateAccountEvent, MergeAccountEvent, OperationEvent, PaymentEvent}

/** The result of submitting a transaction to a network */
case class TransactionResponse(
  /** The resulting event for each submitted operation */
  operationEvents: List[OperationEvent],
  /** The cost in stroops for the submitting account */
  feeCharged: Long,
  /** Whether the transaction was accepted by the network and included in the ledger */
  accepted: Boolean
)

object TransactionResponse {
  def apply(
    envelopeXdr: String,
    resultXdr: String
  ): TransactionResponse = {
    val result: TransactionResult = TransactionResult.decode(ByteString.decodeBase64(resultXdr))
    val envelope: TransactionEnvelope = TransactionEnvelope.decode(ByteString.decodeBase64(envelopeXdr))
    val feeCharged: Long = result.getFeeCharged.getInt64
    val operationEvents: List[OperationEvent] = {
      val (sourceAccount, operationsRequested) = envelope.getDiscriminant match {
        // case EnvelopeType.ENVELOPE_TYPE_TX_V0 => envelope.getV0.getTx.getOperations
        case EnvelopeType.ENVELOPE_TYPE_TX =>
          (envelope.getV1.getTx.getSourceAccount, envelope.getV1.getTx.getOperations.toList)
        // case EnvelopeType.ENVELOPE_TYPE_TX_FEE_BUMP => envelope.getFeeBump.getTx.getInnerTx.getV1.getTx.getOperations
        // case default => throw new IllegalStateException(s"Unexpected envelope type: $default")
      }

      val rawResults = result.getResult.getResults.toList

      operationsRequested.zip(rawResults).map {
        case (requested, result) =>
          requested.getBody.getDiscriminant match {
            case ACCOUNT_MERGE => MergeAccountEvent.decode(requested, result, sourceAccount)
            case CREATE_ACCOUNT => CreateAccountEvent.decode(requested, result, sourceAccount)
            case PAYMENT => PaymentEvent.decode(requested, result, sourceAccount)
            // case default => throw new IllegalStateException(s"Unexpected operation type: $default")
          }
      }
    }

    TransactionResponse(
      operationEvents = operationEvents,
      feeCharged = feeCharged,
      accepted = operationEvents.forall(_.accepted)
    )
  }
}