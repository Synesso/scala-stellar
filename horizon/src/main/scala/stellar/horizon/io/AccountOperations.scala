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


trait AllOperation[F[_]] {
  def accountOperations: AccountOperations[F]
  def passwordOperations: PasswordOperations[F]
}


object Program {
  def resetPassword(accountId: AccountId, passwordInput: PasswordInput)
    (implicit ops: AllOperation): Future[AccountDetail] = {
    import ops._

    for {
      accountDetail <- accountOperations.accountDetail(accountId)
      validatedPasswordInput <- passwordResetOperation.validate(passwordInput)
      _ <- passwordResetOperation.reset(accountDetail, validatedPasswordInput)
    }
  }
}