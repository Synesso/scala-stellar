package stellar.protocol.result

import cats.data.State
import stellar.protocol.xdr.Encode.long
import stellar.protocol.xdr.{Decoder, Encodable, Encode}
import stellar.protocol.{AccountId, Amount}

case class OfferClaim(seller: AccountId, offerId: Long, sold: Amount, bought: Amount) extends Encodable {
  def encode: LazyList[Byte] = seller.encode ++ long(offerId) ++ sold.encode ++ bought.encode
}

object OfferClaim extends Decoder[OfferClaim] {
  val decodeOld: State[Seq[Byte], OfferClaim] = for {
    seller <- AccountId.decodeOld
    offerId <- long
    sold <- Amount.decodeOld
    bought <- Amount.decodeOld
  } yield OfferClaim(seller, offerId, sold, bought)
}