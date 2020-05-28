package stellar.horizon.io

object AccountOperationsInterpreter{
/*

}

trait HttpExchange[F[_]] {
  def invoke(request: Request): F[Response]
}

class HttpExchangeAsync extends HttpExchange[Future] {
  override def invoke(httpRequest: Request): Future[Response] = ???
}

object MockHttpExchange {
  case class Invoke(request: Request) extends Call
  sealed trait Call
}

class MockHttpExchange(
  mockInvoke: Invoke => Future[Response]
) extends HttpExchange[Future] {
  var calls: Seq[Call] = Seq.empty

  override def invoke(httpRequest: Request): Future[Response] = {
    val call = Invoke(httpRequest)
    calls :+= call
    mockInvoke(call)
  }

}

class AccountOperationsInterpreter(
  baseUri: String,
  httpExchange: HttpExchange[Future]
)(implicit ec: ExecutionContext) extends AccountOperations[Future] {
  override def accountDetail(accountId: AccountId): Future[AccountDetail] = {
    val request = _

    for {
      response  <- httpExchange.invoke(request)
      accountDetails <- Future {
        //deserialize json from response
      }
    } yield accountDetails
  }
*/
}
