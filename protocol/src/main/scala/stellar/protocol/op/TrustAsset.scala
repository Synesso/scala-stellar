package stellar.protocol.op

import org.stellar.xdr
import org.stellar.xdr.Operation.OperationBody
import org.stellar.xdr.{ChangeTrustOp, Int64, OperationType}
import stellar.protocol.{Address, Token}

/**
 * An operation to set a new level of trust between an account and an asset.
 *
 * @see See [[https://developers.stellar.org/docs/start/list-of-operations/#change-trust the official guide]] for
 *      details on this operation.
 * @param asset  the asset having its trust limit adjusted
 * @param limit  how much of the asset is the account willing to hold
 * @param source the account establishing the new trust limit
 */
case class TrustAsset(
  asset: Token,
  limit: Long,
  source: Option[Address] = None
) extends Operation {
  override def xdrEncode: xdr.Operation = {
    new xdr.Operation.Builder()
      .body(new OperationBody.Builder()
        .discriminant(OperationType.CHANGE_TRUST)
        .changeTrustOp(new ChangeTrustOp.Builder()
          .limit(new Int64(limit))
          .line(asset.xdrEncode)
          .build())
        .build())
      .sourceAccount(source.map(_.xdrEncodeMultiplexed).orNull)
      .build()
  }
}

object TrustAsset {
  def removeTrust(
    asset: Token,
    source: Option[Address] = None
  ) = TrustAsset(asset, 0L, source)
}
