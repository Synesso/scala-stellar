package stellar.event

import org.stellar.xdr.CreateAccountResultCode.CREATE_ACCOUNT_SUCCESS
import org.stellar.xdr.PaymentResultCode.{PAYMENT_SUCCESS, PAYMENT_UNDERFUNDED}
import org.stellar.xdr.{AccountMergeResultCode, ChangeTrustResultCode, MuxedAccount, Operation, OperationResult, OperationResultCode}
import stellar.protocol.Address

sealed trait OperationEvent {
  val source: Address
  val accepted: Boolean
}

trait CreateAccountEvent extends OperationEvent
object CreateAccountEvent {
  def decode(
    requested: Operation,
    result: OperationResult,
    source: MuxedAccount
  ): CreateAccountEvent = {
    result.getDiscriminant match {
      case OperationResultCode.opINNER =>
        result.getTr.getCreateAccountResult.getDiscriminant match {
          case CREATE_ACCOUNT_SUCCESS => AccountCreated.decode(
            op = requested.getBody.getCreateAccountOp,
            source = Option(requested.getSourceAccount).getOrElse(source)
          )
        }
    }
  }
}

trait PaymentEvent extends OperationEvent
object PaymentEvent {
  def decode(
    requested: Operation,
    result: OperationResult,
    source: MuxedAccount
  ): PaymentEvent = {
    result.getDiscriminant match {
      case OperationResultCode.opINNER =>
        result.getTr.getPaymentResult.getDiscriminant match {
          case PAYMENT_SUCCESS => PaymentMade.decode(
            op = requested.getBody.getPaymentOp,
            source = Option(requested.getSourceAccount).getOrElse(source)
          )
          case paymentResult => PaymentFailed.decode(
            op = requested.getBody.getPaymentOp,
            source = Option(requested.getSourceAccount).getOrElse(source),
            failure = paymentResult
          )
        }
    }
  }
}

trait MergeAccountEvent extends OperationEvent
object MergeAccountEvent {
  def decode(
    requested: Operation,
    result: OperationResult,
    source: MuxedAccount
  ): MergeAccountEvent = {
    result.getDiscriminant match {
      case OperationResultCode.opINNER =>
        result.getTr.getAccountMergeResult.getDiscriminant match {
          case AccountMergeResultCode.ACCOUNT_MERGE_SUCCESS => AccountMerged.decode(
            source = Option(requested.getSourceAccount).getOrElse(source),
            destination = requested.getBody.getDestination,
            amount = result.getTr.getAccountMergeResult.getSourceAccountBalance
          )
        }
    }
  }
}

trait TrustChangeEvent extends OperationEvent
object TrustChangeEvent {
  def decode(
    requested: Operation,
    result: OperationResult,
    source: MuxedAccount
  ): TrustChangeEvent = {
    result.getDiscriminant match {
      case OperationResultCode.opINNER =>
        result.getTr.getChangeTrustResult.getDiscriminant match {
          case ChangeTrustResultCode.CHANGE_TRUST_SUCCESS => TrustChanged.decode(
            op = requested.getBody.getChangeTrustOp,
            source = Option(requested.getSourceAccount).getOrElse(source)
          )
          case changeTrustResultCode => TrustChangeFailed.decode(
            op = requested.getBody.getChangeTrustOp,
            source = Option(requested.getSourceAccount).getOrElse(source),
            failure = changeTrustResultCode
          )
        }
    }
  }
}