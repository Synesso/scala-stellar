package stellar.protocol.ledger

import cats.data.State
import stellar.protocol.{AccountId, Asset, Token}
import stellar.protocol.xdr.{Decoder, Encodable, Encode}

sealed trait LedgerKey extends Encodable

object LedgerKey extends Decoder[LedgerKey] {

  val decodeOld: State[Seq[Byte], LedgerKey] = switch[LedgerKey](
    widen(AccountKey.decodeOld),
    widen(TrustLineKey.decodeOld),
    widen(OfferKey.decodeOld),
    widen(DataKey.decodeOld)
  )
}

case class AccountKey(accountId: AccountId) extends LedgerKey {
  override def encode: LazyList[Byte] = Encode.int(0) ++ accountId.encode
}

object AccountKey extends Decoder[AccountKey] {
  val decodeOld: State[Seq[Byte], AccountKey] = AccountId.decodeOld.map(AccountKey(_))
}

case class TrustLineKey(accountId: AccountId, token: Token) extends LedgerKey {
  override def encode: LazyList[Byte] = Encode.int(1) ++ accountId.encode ++ token.encode
}

object TrustLineKey extends Decoder[TrustLineKey] {
  val decodeOld: State[Seq[Byte], TrustLineKey] = for {
    account <- AccountId.decodeOld
    asset <- Asset.decodeOld.map(_.asInstanceOf[Token])
  } yield TrustLineKey(account, asset)
}

case class OfferKey(accountId: AccountId, offerId: Long) extends LedgerKey {
  override def encode: LazyList[Byte] = Encode.int(2) ++ accountId.encode ++ Encode.long(offerId)
}

object OfferKey extends Decoder[OfferKey] {
  val decodeOld: State[Seq[Byte], OfferKey] = for {
    account <- AccountId.decodeOld
    offerId <- long
  } yield OfferKey(account, offerId)
}

case class DataKey(accountId: AccountId, name: String) extends LedgerKey {
  override def encode: LazyList[Byte] = Encode.int(3) ++ accountId.encode ++ Encode.string(name)
}

object DataKey extends Decoder[DataKey] {
  val decodeOld: State[Seq[Byte], DataKey] = for {
    account <- AccountId.decodeOld
    name <- string
  } yield DataKey(account, name)
}
