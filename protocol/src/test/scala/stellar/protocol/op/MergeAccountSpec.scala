package stellar.protocol.op

import org.specs2.mutable.Specification
import stellar.protocol.{AccountId, Address, Lumen}

class MergeAccountSpec extends Specification {

  "account merging" should {
    "encode correctly" >> {
      MergeAccount(Address("GBUW2YWFIUR45JOA7DKN746EUB6G53VSRFCLNP442Z66I4ERSFPJ6KQI"))
        .xdrEncode.encode().base64() must
        beEqualTo("AAAAAAAAAAgAAAAAaW1ixUUjzqXA+NTf88SgfG7usolEtr+c1n3kcJGRXp8=")
    }

    "encode with an explicit source account" >> {
      MergeAccount(
        destination = Address("GBUW2YWFIUR45JOA7DKN746EUB6G53VSRFCLNP442Z66I4ERSFPJ6KQI"),
        source = Some(Address(
          address = "GCKIBOAMMHGDZI5UNCUUWXSUBENHZ65AYM4MYFEOGRMLB4IZXC63WZDR",
          memoId = 88_888_888L
        ))
      ).xdrEncode.encode().base64() must
        beEqualTo("AAAAAQAAAQAAAAAABUxWOJSAuAxhzDyjtGipS15UCRp8+6DDOMwUjjRYsPEZuL27AAAACAAAAABpbWLFRSPOpcD41N" +
          "/zxKB8bu6yiUS2v5zWfeRwkZFenw==")
    }
  }

}
