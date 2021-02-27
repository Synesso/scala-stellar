package stellar.protocol

import cats.data.State
import stellar.protocol.xdr.Encode.{bytes, int}
import stellar.protocol.xdr.{Decoder, Encodable}

/**
 * An Address is how accounts are identified.
 *
 * @param accountId The public component of an account on the network.
 */
case class Address(accountId: AccountId) extends Encodable {

  override def encode: LazyList[Byte] = int(0x000) ++ bytes(32, accountId.hash)

}

object Address extends Decoder[Address] {
  override val decodeOld: State[Seq[Byte], Address] = int.flatMap {
    case 0x000 => byteString(32).map(bs => Address(AccountId(bs)))
  }

  def apply(address: String): Address = {
    require(address.headOption.contains('G'))
    Address(AccountId(address))
  }
}