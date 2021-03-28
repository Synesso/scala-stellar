package stellar.protocol

import java.nio.charset.StandardCharsets

import okio.ByteString
import org.stellar.xdr
import org.stellar.xdr.Asset.{AssetAlphaNum12, AssetAlphaNum4}
import org.stellar.xdr.{AssetCode12, AssetCode4, AssetType}

sealed trait Asset {
  val code: String

  def xdrEncode: xdr.Asset
}

object Asset {
  def decode(x: xdr.Asset): Asset = {
    x.getDiscriminant match {
      case AssetType.ASSET_TYPE_NATIVE => Lumen
      case AssetType.ASSET_TYPE_CREDIT_ALPHANUM4 =>
        Token(
          code = new String(x.getAlphaNum4.getAssetCode.getAssetCode4, StandardCharsets.UTF_8).trim(),
          issuer = AccountId.decode(x.getAlphaNum4.getIssuer)
        )
      case AssetType.ASSET_TYPE_CREDIT_ALPHANUM12 =>
        Token(
          code = new String(x.getAlphaNum12.getAssetCode.getAssetCode12, StandardCharsets.UTF_8).trim(),
          issuer = AccountId.decode(x.getAlphaNum12.getIssuer)
        )
    }
  }
}

/**
 * The network's native asset, XLM.
 */
case object Lumen extends Asset {
  val STROOPS_PER_LUMEN = 10_000_000L

  override val code: String = "XLM"
  def apply(lumen: Int): Amount = stroops(lumen * STROOPS_PER_LUMEN)
  def stroops(stroops: Long): Amount = Amount(Lumen, stroops)

  override def xdrEncode: xdr.Asset = new xdr.Asset.Builder()
    .discriminant(AssetType.ASSET_TYPE_NATIVE)
    .build()
}

/**
 * An account-defined custom asset.
 */
case class Token(code: String, issuer: AccountId) extends Asset {
  private val codeBytes = new ByteString(code.getBytes(StandardCharsets.UTF_8))
  private val size = codeBytes.size()
  private val isCompact = size <= 4
  require(size >= 1 && size <= 12)

  override def xdrEncode: xdr.Asset = {
    val codeBytes = code.getBytes(StandardCharsets.UTF_8)
    new xdr.Asset.Builder()
      .discriminant(if (isCompact) AssetType.ASSET_TYPE_CREDIT_ALPHANUM4 else AssetType.ASSET_TYPE_CREDIT_ALPHANUM12)
      .alphaNum4(if (isCompact) new AssetAlphaNum4.Builder()
        .assetCode(new AssetCode4(codeBytes.padTo(4, 0)))
        .issuer(issuer.xdrEncode)
        .build() else null)
      .alphaNum12(if (isCompact) null else new AssetAlphaNum12.Builder()
        .assetCode(new AssetCode12(codeBytes.padTo(12, 0)))
        .issuer(issuer.xdrEncode)
        .build())
      .build()
  }

}

object Token {
  def decode(asset: xdr.Asset): Token = Asset.decode(asset).asInstanceOf[Token]
}
