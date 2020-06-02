package stellar.protocol

import cats.data.State
import net.i2p.crypto.eddsa.{EdDSAPublicKey, KeyPairGenerator}
import okio.ByteString
import org.apache.commons.codec.binary.Base32
import stellar.protocol.Key.codec
import stellar.protocol.xdr.{ByteArrays, Decoder, Encodable, Encode}

/**
 * A Key (also known as a StrKey, or Stellar Key) is a typed, encoded byte array.
 */
sealed trait Key {
  val kind: Byte
  val hash: ByteString
  def checksum: ByteString = ByteArrays.checksum(kind +: hash.toByteArray)
  lazy val encodeToString: String = codec.encode(kind +: hash.toByteArray ++: checksum.toByteArray)
    .map(_.toChar).mkString
}

object Key {
  val codec = new Base32()

  def decodeFromString(key: String): ByteString = decodeFromChars(key.toList)
  def decodeFromChars(key: Seq[Char]): ByteString = {
    assert(key.forall(_ <= 127), s"Illegal characters in provided StrKey")

    val bytes = key.map(_.toByte).toArray
    val decoded: Array[Byte] = codec.decode(bytes)
    assert(decoded.length == 35, s"Incorrect length. Expected 35 bytes, got ${decoded.length} in StrKey: $key")

    val data = decoded.tail.take(32)
    val Array(sumA, sumB) = decoded.drop(33)
    val Array(checkA, checkB) = ByteArrays.checksum(decoded.take(33)).toByteArray
    assert((checkA, checkB) == (sumA, sumB),
      f"Checksum does not match. Provided: 0x$sumA%04X0x$sumB%04X. Actual: 0x$checkA%04X0x$checkB%04X")
    new ByteString(data)
  }
}

/**
 * Only a subset of Keys can be signers. Seeds should not be the declared signer
 * (as they are the private dual of the AccountId).
 */
sealed trait SignerKey extends Key with Encodable

object SignerKey extends Decoder[SignerKey] {
  override val decode: State[Seq[Byte], SignerKey] = switch(
    byteString(32).map(AccountId(_)),
    byteString(32).map(PreAuthTx(_)),
    byteString(32).map(HashX(_))
  )
}

/**
 * The public facing identifier of a stellar key pair. The string encoded form starts with
 * a G. If the account includes a subAccountId then the encoded form may start with an M and
 * include that sub account id.
 */
case class AccountId(hash: ByteString) extends SignerKey {
  val kind: Byte = (6 << 3).toByte // G
  def encode: LazyList[Byte] = Encode.int(0) ++ Encode.bytes(32, hash)

  override def toString: String = s"AccountId($encodeToString)"
}

object AccountId extends Decoder[AccountId] {
  private val generator = new KeyPairGenerator()

  val decode: State[Seq[Byte], AccountId] = for {
    _ <- int
    bs <- byteString(32)
  } yield AccountId(bs)

  def apply(accountId: String): AccountId = AccountId(Key.decodeFromString(accountId))

  // TODO (jem) -> This is more useful if it comes from a random secret seed, which does not exist yet.
  def random: AccountId = AccountId(new ByteString(generator.generateKeyPair().getPublic().asInstanceOf[EdDSAPublicKey].getAbyte))
}

/**
 * The private dual of the account id. Seeds are not encodable, because they are never transmitted.
 */
case class Seed(hash: ByteString) extends Key {
  val kind: Byte = (18 << 3).toByte // S
}

object Seed {
  def apply(secret: String): Seed = {
    assert(secret.startsWith("S"))
    Seed(Key.decodeFromString(secret))
  }
}

/**
 * Pre-authorized transactions keys are the hashes of yet to be transmitted transactions. Signers
 * of this kind are automatically removed from the account when the transaction is accepted by the
 * network. See https://www.stellar.org/developers/guides/concepts/multi-sig.html#pre-authorized-transaction
 */
case class PreAuthTx(hash: ByteString) extends SignerKey {
  val kind: Byte = (19 << 3).toByte // T
  def encode: LazyList[Byte] = Encode.int(1) ++ Encode.bytes(32, hash)
}

object PreAuthTx extends Decoder[PreAuthTx] {
  val decode: State[Seq[Byte], PreAuthTx] = for {
    _ <- int
    bs <- bytes(32)
  } yield PreAuthTx(new ByteString(bs.toArray))

  def apply(hash: String): PreAuthTx = {
    assert(hash.startsWith("T"))
    PreAuthTx(Key.decodeFromString(hash))
  }
}

/**
 * Arbitrary 256-byte values can be used as signatures on transactions. The hash of such value are
 * able to be used as signers. See https://www.stellar.org/developers/guides/concepts/multi-sig.html#hashx
 */
case class HashX(hash: ByteString) extends SignerKey {
  val kind: Byte = (23 << 3).toByte // X
  def encode: LazyList[Byte] = Encode.int(2) ++ Encode.bytes(32, hash)
}

object HashX extends Decoder[HashX] {
  val decode: State[Seq[Byte], HashX] = for {
    _ <- int
    bs <- bytes(32)
  } yield HashX(new ByteString(bs.toArray))

  def apply(hash: String): HashX = {
    assert(hash.startsWith("X"))
    HashX(Key.decodeFromString(hash))
  }
}