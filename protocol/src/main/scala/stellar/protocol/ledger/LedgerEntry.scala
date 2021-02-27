package stellar.protocol.ledger

import cats.data.State
import stellar.protocol.xdr.Encode.int
import stellar.protocol.xdr.{Decoder, Encodable}

case class LedgerEntry(lastModifiedLedgerSeq: Int, data: LedgerEntryData, private val dataDisc: Int) extends Encodable {
  override def encode: LazyList[Byte] = int(lastModifiedLedgerSeq) ++ data.encodeDiscriminated
}

object LedgerEntry extends Decoder[LedgerEntry] {
  val decodeOld: State[Seq[Byte], LedgerEntry] = for {
    lastModifiedLedgerSeq <- int
    dataDisc <- switchInt[LedgerEntryData](
      widen(AccountEntry.decodeOld),
      widen(TrustLineEntry.decodeOld),
      widen(OfferEntry.decodeOld),
      widen(DataEntry.decodeOld)
    )
    (data, disc) = dataDisc
  } yield LedgerEntry(lastModifiedLedgerSeq, data, disc)
}