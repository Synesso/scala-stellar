package stellar.protocol.op

import cats.data.State
import stellar.protocol.xdr.Decoder
import stellar.protocol.xdr.Encode.{int, opt}
import stellar.protocol.{Address, Amount}

/**
 * A payment operation.
 *
 * @param sender    the issuer of the funds. If absent, this issuer will be the source of the transaction.
 * @param recipient the receiver of the funds
 * @param amount    the amount to be sent
 */
case class Payment(sender: Option[Address], recipient: Address, amount: Amount) extends Operation {
  override def encode: LazyList[Byte] = opt(sender) ++ int(1) ++ recipient.encode ++ amount.encode
}

object Payment extends Decoder[Payment] {
  override val decode: State[Seq[Byte], Payment] = for {
    sender <- opt(Address.decode)
    _ <- int
    recipient <- Address.decode
    amount <- Amount.decode
  } yield Payment(sender, recipient, amount)
}
