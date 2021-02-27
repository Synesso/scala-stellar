package stellar.horizon

import okio.ByteString
import org.stellar.xdr._
import stellar.event.{AccountCreated, OperationEvent}
import stellar.protocol.{AccountId, Amount, Lumen}

case class TransactionResponse(
  hash: String,
  ledger: Long,
  envelopeXdr: String,
  resultXdr: String,
  resultMetaXdr: String
) {

  private lazy val result: TransactionResult = TransactionResult.decode(ByteString.decodeBase64(resultXdr))
  private lazy val envelope: TransactionEnvelope = TransactionEnvelope.decode(ByteString.decodeBase64(envelopeXdr))

  def feeCharged: Amount = Lumen.stroops(result.getFeeCharged.getInt64)

  def operationEvents: List[OperationEvent] = {
    val operationsRequested = (envelope.getDiscriminant match {
      // case EnvelopeType.ENVELOPE_TYPE_TX_V0 => envelope.getV0.getTx.getOperations
      case EnvelopeType.ENVELOPE_TYPE_TX => envelope.getV1.getTx.getOperations
      // case EnvelopeType.ENVELOPE_TYPE_TX_FEE_BUMP => envelope.getFeeBump.getTx.getInnerTx.getV1.getTx.getOperations
      case default => throw new IllegalStateException(s"Unexpected envelope type: $default")
    }).toList

    val rawResults = result.getResult.getResults.toList

    operationsRequested.zip(rawResults).map {
      case (requested, result) =>
        requested.getBody.getDiscriminant match {
          case OperationType.CREATE_ACCOUNT =>
            result.getDiscriminant match {
              case OperationResultCode.opINNER =>
                result.getTr.getCreateAccountResult.getDiscriminant match {
                  case CreateAccountResultCode.CREATE_ACCOUNT_SUCCESS =>
                    val op = requested.getBody.getCreateAccountOp
                    AccountCreated(
                      accountId = AccountId.decode(op.getDestination.getAccountID),
                      startingBalance = op.getStartingBalance.getInt64,
                      fundingAccountId = AccountId.decode(requested.getSourceAccount)
                    )
                }
            }
          case default => throw new IllegalStateException(s"Unexpected operation type: $default")
        }
    }
  }
}