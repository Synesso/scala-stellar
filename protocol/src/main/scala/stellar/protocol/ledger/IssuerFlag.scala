package stellar.protocol.ledger

import cats.data.State
import stellar.protocol.xdr.Decoder

sealed trait IssuerFlag {
  val i: Int
  val s: String
}

case object AuthorizationRequiredFlag extends IssuerFlag {
  val i = 0x1
  val s = "auth_required_flag"
}

case object AuthorizationRevocableFlag extends IssuerFlag {
  val i = 0x2
  val s = "auth_revocable_flag"
}

case object AuthorizationImmutableFlag extends IssuerFlag {
  val i = 0x4
  val s = "auth_immutable_flag"
}

object IssuerFlagSet extends Decoder[Set[IssuerFlag]] {
  val all: Set[IssuerFlag] = Set(AuthorizationRequiredFlag, AuthorizationRevocableFlag, AuthorizationImmutableFlag)

  // def apply(i: Int): Option[IssuerFlag] = all.find(_.i == i)

  def from(i: Int): Set[IssuerFlag] = all.filter { f => (i & f.i) == f.i }

  val decodeOld: State[Seq[Byte], Set[IssuerFlag]] = int.map(from)
}

