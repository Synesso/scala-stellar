package stellar.protocol.result

import cats.data.State
import stellar.protocol.xdr.{Decoder, Encode}

sealed abstract class CreateAccountResult(opResultCode: Int) extends OpResult {
  override val opCode: Int = 0
  override def encode: LazyList[Byte] = Encode.int(opResultCode)
}

object CreateAccountResult extends Decoder[CreateAccountResult] {
  val decodeOld: State[Seq[Byte], CreateAccountResult] = int.map {
    case 0 => CreateAccountSuccess
    case -1 => CreateAccountMalformed
    case -2 => CreateAccountUnderfunded
    case -3 => CreateAccountLowReserve
    case -4 => CreateAccountAlreadyExists
  }
}

/**
 * CreateAccount operation was successful.
 */
case object CreateAccountSuccess extends CreateAccountResult(0)

/**
 * CreateAccount operation failed because the destination account was malformed.
 */
case object CreateAccountMalformed extends CreateAccountResult(-1)

/**
 * CreateAccount operation failed because there was insufficient funds in the source account.
 */
case object CreateAccountUnderfunded extends CreateAccountResult(-2)

/**
 * CreateAccount operation failed because there was insufficient funds sent to cover the base reserve.
 */
case object CreateAccountLowReserve extends CreateAccountResult(-3)

/**
 * CreateAccount operation failed because the destination account already exists.
 */
case object CreateAccountAlreadyExists extends CreateAccountResult(-4)
