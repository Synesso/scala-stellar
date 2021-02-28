package stellar.protocol.op

import org.stellar.xdr
import org.stellar.xdr.Operation.OperationBody
import org.stellar.xdr.OperationType
import stellar.protocol.Address

/**
 * A merge (close) account operation.
 *
 * @see See [[https://developers.stellar.org/docs/start/list-of-operations/#account-merge the official guide]] for
 *      details on this operation.
 * @param destination The address of the account to receive the balance of funds.
 * @param source      Optionally, a source account if it should be different to that in the containing Transaction. This
 *                    is the account that will be closed.
 */
case class MergeAccount(
  destination: Address,
  source: Option[Address] = None
) extends Operation {
  override def xdrEncode: xdr.Operation = new xdr.Operation.Builder()
    .body(new OperationBody.Builder()
      .discriminant(OperationType.ACCOUNT_MERGE)
      .destination(destination.xdrEncodeMultiplexed)
      .build())
    .sourceAccount(source.map(_.xdrEncodeMultiplexed).orNull)
    .build()
}