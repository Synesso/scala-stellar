package stellar.protocol.ledger

import cats.data.State
import stellar.protocol.xdr.Encode.{int, long}
import stellar.protocol.xdr.{Decoder, Encodable}

case class LiabilitySum(buying: Long, selling: Long) extends Encodable {
  override def encode: LazyList[Byte] = long(buying) ++ long(selling) ++ int(0)
}

object LiabilitySum extends Decoder[LiabilitySum] {
  val decodeOld: State[Seq[Byte], LiabilitySum] = for {
    buying <- long
    selling <- long
    _ <- int
  } yield LiabilitySum(buying, selling)
}

