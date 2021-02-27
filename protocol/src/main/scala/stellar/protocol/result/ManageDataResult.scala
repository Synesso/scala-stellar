package stellar.protocol.result

import cats.data.State
import stellar.protocol.xdr.Decoder
import stellar.protocol.xdr.Encode.int

sealed abstract class ManageDataResult(val opResultCode: Int) extends OpResult {
  override val opCode: Int = 4
  override def encode: LazyList[Byte] = int(opResultCode)
}

object ManageDataResult extends Decoder[ManageDataResult] {
  val decodeOld: State[Seq[Byte], ManageDataResult] = int.map {
    case 0 => ManageDataSuccess
    case -1 => ManageDataNotSupportedYet
    case -2 => DeleteDataNameNotFound
    case -3 => AddDataLowReserve
    case -4 => AddDataInvalidName
  }
}

/**
 * ManageData operation was successful.
 */
case object ManageDataSuccess extends ManageDataResult(0)

/**
 * ManageData operation failed because the network was not yet prepared to support this operation.
 */
case object ManageDataNotSupportedYet extends ManageDataResult(-1)

/**
 * ManageData operation failed because there was no data entry with the given name.
 */
case object DeleteDataNameNotFound extends ManageDataResult(-2)

/**
 * ManageData operation failed because there was insufficient reserve to support the addition of a new data entry.
 */
case object AddDataLowReserve extends ManageDataResult(-3)

/**
 * ManageData operation failed because the name was not a valid string.
 */
case object AddDataInvalidName extends ManageDataResult(-4)
