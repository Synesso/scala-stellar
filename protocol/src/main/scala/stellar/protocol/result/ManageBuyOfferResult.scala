package stellar.protocol.result

import cats.data.State
import stellar.protocol.ledger.OfferEntry
import stellar.protocol.xdr.Decoder
import stellar.protocol.xdr.Encode.{arr, int}

sealed abstract class ManageBuyOfferResult(val opResultCode: Int) extends OpResult {
  override val opCode: Int = 12
  override def encode: LazyList[Byte] = int(opResultCode)
}

object ManageBuyOfferResult extends Decoder[ManageBuyOfferResult] {
  val decodeOld: State[Seq[Byte], ManageBuyOfferResult] = int.flatMap {
    case 0 => for {
      claims <- arr(OfferClaim.decodeOld)
      result <- switch[ManageBuyOfferResult](
        widen(OfferEntry.decodeOld.map(entry => ManageBuyOfferCreated(claims, entry))),
        widen(OfferEntry.decodeOld.map(entry => ManageBuyOfferUpdated(claims, entry))),
        widen(State.pure(ManageBuyOfferDeleted(claims)))
      )
    } yield result
    case -1 => State.pure(ManageBuyOfferMalformed)
    case -2 => State.pure(ManageBuyOfferSellNoTrust)
    case -3 => State.pure(ManageBuyOfferBuyNoTrust)
    case -4 => State.pure(ManageBuyOfferSellNoAuth)
    case -5 => State.pure(ManageBuyOfferBuyNoAuth)
    case -6 => State.pure(ManageBuyOfferLineFull)
    case -7 => State.pure(ManageBuyOfferUnderfunded)
    case -8 => State.pure(ManageBuyOfferCrossSelf)
    case -9 => State.pure(ManageBuyOfferSellNoIssuer)
    case -10 => State.pure(ManageBuyOfferBuyNoIssuer)
    case -11 => State.pure(UpdateBuyOfferIdNotFound)
    case -12 => State.pure(ManageBuyOfferLowReserve)
  }
}

/**
 * ManageBuyOffer operation was successful and an offer was created.
 *
 * @param claims the trades that were effected as a result of posting this offer.
 * @param entry the offer entry that was newly created.
 */
case class ManageBuyOfferCreated(claims: List[OfferClaim], entry: OfferEntry) extends ManageBuyOfferResult(0) {
  override def encode: LazyList[Byte] = super.encode ++ arr(claims) ++ int(0) ++ entry.encode
}

/**
 * ManageBuyOffer operation was successful and an offer was updated.
 *
 * @param claims the trades that were effected as a result of posting this offer.
 * @param entry the offer entry that was newly updated.
 */
case class ManageBuyOfferUpdated(claims: List[OfferClaim], entry: OfferEntry) extends ManageBuyOfferResult(0) {
  override def encode: LazyList[Byte] = super.encode ++ arr(claims) ++ int(1) ++ entry.encode
}

/**
 * ManageBuyOffer operation was successful and an offer was deleted.
 *
 * @param claims the trades that were effected as a result of posting this offer.
 */
case class ManageBuyOfferDeleted(claims: List[OfferClaim]) extends ManageBuyOfferResult(0) {
  override def encode: LazyList[Byte] = super.encode ++ arr(claims) ++ int(2)
}

/**
 * ManageBuyOffer operation failed because the request was malformed.
 * E.g. Either of the assets were invalid, the assets were the same as each other,
 * the amount was less than zero, or the price numerator or denominator were zero or less.
 */
case object ManageBuyOfferMalformed extends ManageBuyOfferResult(-1)

/**
 * ManageBuyOffer operation failed because there was no trustline for what was being offered.
 * (This also implies the account was underfunded).
 */
case object ManageBuyOfferSellNoTrust extends ManageBuyOfferResult(-2)

/**
 * ManageBuyOffer operation failed because there was no trustline for what was being sought.
 */
case object ManageBuyOfferBuyNoTrust extends ManageBuyOfferResult(-3)

/**
 * ManageBuyOffer operation failed because the account is not authorised to sell the offered asset.
 */
case object ManageBuyOfferSellNoAuth extends ManageBuyOfferResult(-4)

/**
 * ManageBuyOffer operation failed because the account is not authorised to buy the sought asset.
 */
case object ManageBuyOfferBuyNoAuth extends ManageBuyOfferResult(-5)

/**
 * ManageBuyOffer operation failed because it would have put the account's balance over the limit for the sought asset.
 */
case object ManageBuyOfferLineFull extends ManageBuyOfferResult(-6)

/**
 * ManageBuyOffer operation failed because there was an insufficient balance of the asset being offered to meet the offer.
 */
case object ManageBuyOfferUnderfunded extends ManageBuyOfferResult(-7)

/**
 * ManageBuyOffer operation failed because it would have matched with an offer from the same account.
 */
case object ManageBuyOfferCrossSelf extends ManageBuyOfferResult(-8)

/**
 * ManageBuyOffer operation failed because there is no issuer for the asset being offered.
 */
case object ManageBuyOfferSellNoIssuer extends ManageBuyOfferResult(-9)

/**
 * ManageBuyOffer operation failed because there is no issuer for the asset being sought.
 */
case object ManageBuyOfferBuyNoIssuer extends ManageBuyOfferResult(-10)

/**
 * ManageBuyOffer operation failed because it was an update attempt, but an offer with the given id did not exist.
 */
case object UpdateBuyOfferIdNotFound extends ManageBuyOfferResult(-11)

/**
 * ManageBuyOffer operation failed because the cumulative amount of it & all current offers from the same account
 * would exceed the account's available balance.
 */
case object ManageBuyOfferLowReserve extends ManageBuyOfferResult(-12)


