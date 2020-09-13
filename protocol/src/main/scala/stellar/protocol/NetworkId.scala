package stellar.protocol

import okio.ByteString

case class NetworkId(passphrase: String) {
  val hash: LazyList[Byte] = LazyList.from(ByteString.encodeUtf8(passphrase).sha256().toByteArray.toList)
}
