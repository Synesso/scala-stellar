package stellar.horizon.io

import okhttp3.{Request, Response}

/**
 * For testing, declare the behaviour when invocations are made and record the requests.
 * @param fakeInvocation how to handle the requests made.
 * @tparam F the effect to return the response in
 */
class FakeHttpExchange[F[_]](
  fakeInvocation: Request => F[Response]
) extends HttpExchange[F] {
  var requests: List[Request] = Nil
  override def invoke(request: Request): F[Response] = {
    requests :+= request
    fakeInvocation(request)
  }
}
