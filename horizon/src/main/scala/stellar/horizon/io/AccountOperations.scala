package stellar.horizon.io

import stellar.horizon.AccountDetail
import stellar.protocol.AccountId

import scala.concurrent.Future
import scala.util.Try

object AccountOperations {
  def blocking(): AccountOperations[Try] = ???
  def async(): AccountOperations[Future] = ???
}

trait AccountOperations[F[_]] {
  def accountDetail(accountId: AccountId): F[AccountDetail]
}
