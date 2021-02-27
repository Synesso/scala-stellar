package stellar.protocol.result

import cats.data.State
import stellar.protocol.ledger.OfferEntry
import stellar.protocol.xdr.Decoder
import stellar.protocol.xdr.Encode.{arr, int}

sealed abstract class ManageSellOfferResult(val opResultCode: Int) extends OpResult {
  override val opCode: Int = 3
  override def encode: LazyList[Byte] = int(opResultCode)
}

object ManageSellOfferResult extends Decoder[ManageSellOfferResult] {
  val decodeOld: State[Seq[Byte], ManageSellOfferResult] = int.flatMap {
    case 0 => for {
      claims <- arr(OfferClaim.decodeOld)
      result <- switch[ManageSellOfferResult](
        widen(OfferEntry.decodeOld.map(entry => ManageSellOfferCreated(claims, entry))),
        widen(OfferEntry.decodeOld.map(entry => ManageSellOfferUpdated(claims, entry))),
        widen(State.pure(ManageSellOfferDeleted(claims)))
      )
    } yield result
    case -1 => State.pure(ManageSellOfferMalformed)
    case -2 => State.pure(ManageSellOfferSellNoTrust)
    case -3 => State.pure(ManageSellOfferBuyNoTrust)
    case -4 => State.pure(ManageSellOfferSellNoAuth)
    case -5 => State.pure(ManageSellOfferBuyNoAuth)
    case -6 => State.pure(ManageSellOfferLineFull)
    case -7 => State.pure(ManageSellOfferUnderfunded)
    case -8 => State.pure(ManageSellOfferCrossSelf)
    case -9 => State.pure(ManageSellOfferSellNoIssuer)
    case -10 => State.pure(ManageSellOfferBuyNoIssuer)
    case -11 => State.pure(UpdateSellOfferIdNotFound)
    case -12 => State.pure(ManageSellOfferLowReserve)
  }
}

/**
 * ManageSellOffer operation was successful and an offer was created.
 *
 * @param claims the trades that were effected as a result of posting this offer.
 * @param entry the offer entry that was newly created.
 */
case class ManageSellOfferCreated(claims: List[OfferClaim], entry: OfferEntry) extends ManageSellOfferResult(0) {
  override def encode: LazyList[Byte] = super.encode ++ arr(claims) ++ int(0) ++ entry.encode
}

/**
 * ManageSellOffer operation was successful and an offer was updated.
 *
 * @param claims the trades that were effected as a result of posting this offer.
 * @param entry the offer entry that was newly updated.
 */
case class ManageSellOfferUpdated(claims: List[OfferClaim], entry: OfferEntry) extends ManageSellOfferResult(0) {
  override def encode: LazyList[Byte] = super.encode ++ arr(claims) ++ int(1) ++ entry.encode
}

/**
 * ManageSellOffer operation was successful and an offer was deleted.
 *
 * @param claims the trades that were effected as a result of posting this offer.
 */
case class ManageSellOfferDeleted(claims: List[OfferClaim]) extends ManageSellOfferResult(0) {
  override def encode: LazyList[Byte] = super.encode ++ arr(claims) ++ int(2)
}

/**
 * ManageSellOffer operation failed because the request was malformed.
 * E.g. Either of the assets were invalid, the assets were the same as each other,
 * the amount was less than zero, or the price numerator or denominator were zero or less.
 */
case object ManageSellOfferMalformed extends ManageSellOfferResult(-1)

/**
 * ManageSellOffer operation failed because there was no trustline for what was being offered.
 * (This also implies the account was underfunded).
 */
case object ManageSellOfferSellNoTrust extends ManageSellOfferResult(-2)

/**
 * ManageSellOffer operation failed because there was no trustline for what was being sought.
 */
case object ManageSellOfferBuyNoTrust extends ManageSellOfferResult(-3)

/**
 * ManageSellOffer operation failed because the account is not authorised to sell the offered asset.
 */
case object ManageSellOfferSellNoAuth extends ManageSellOfferResult(-4)

/**
 * ManageSellOffer operation failed because the account is not authorised to buy the sought asset.
 */
case object ManageSellOfferBuyNoAuth extends ManageSellOfferResult(-5)

/**
 * ManageSellOffer operation failed because it would have put the account's balance over the limit for the sought asset.
 */
case object ManageSellOfferLineFull extends ManageSellOfferResult(-6)

/**
 * ManageSellOffer operation failed because there was an insufficient balance of the asset being offered to meet the offer.
 */
case object ManageSellOfferUnderfunded extends ManageSellOfferResult(-7)

/**
 * ManageSellOffer operation failed because it would have matched with an offer from the same account.
 */
case object ManageSellOfferCrossSelf extends ManageSellOfferResult(-8)

/**
 * ManageSellOffer operation failed because there is no issuer for the asset being offered.
 */
case object ManageSellOfferSellNoIssuer extends ManageSellOfferResult(-9)

/**
 * ManageSellOffer operation failed because there is no issuer for the asset being sought.
 */
case object ManageSellOfferBuyNoIssuer extends ManageSellOfferResult(-10)

/**
 * ManageSellOffer operation failed because it was an update attempt, but an offer with the given id did not exist.
 */
case object UpdateSellOfferIdNotFound extends ManageSellOfferResult(-11)

/**
 * ManageSellOffer operation failed because the cumulative amount of it & all current offers from the same account
 * would exceed the account's available balance.
 */
case object ManageSellOfferLowReserve extends ManageSellOfferResult(-12)


