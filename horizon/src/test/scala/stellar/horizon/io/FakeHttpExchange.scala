package stellar.horizon.io

import okhttp3.{Request, Response}

import scala.concurrent.Future

/**
 * For testing, declare the behaviour when invocations are made and record the requests.
 * @param fakeInvocation how to handle the requests made.
 */
class FakeHttpExchange(
  fakeInvocation: Request => Future[Response]
) extends HttpExchange[Future] {
  var requests: List[Request] = Nil
  override def invoke(request: Request): Future[Response] = {
    requests :+= request
    fakeInvocation(request)
  }
}
