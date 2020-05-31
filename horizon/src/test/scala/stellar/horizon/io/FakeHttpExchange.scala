package stellar.horizon.io

import okhttp3.{MediaType, Protocol, Request, Response, ResponseBody}

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

object FakeHttpExchange {
  private val json = MediaType.get("application/json; charset=utf-8")

  /**
   * Creates a new FakeHttpExchange that responds OK with the provided body, wrapped in the declared effect.
   * @param body the OK response body
   * @param effect how to wrap the response in the effect.
   * @tparam F the effect
   */
  def respondOkJson[F[_]](body: String, effect: Response => F[Response]): FakeHttpExchange[F] = {
    new FakeHttpExchange[F](
      req => effect(new Response.Builder()
        .body(ResponseBody.create(body, json))
        .code(200)
        .request(req)
        .protocol(Protocol.HTTP_2)
        .message("OK")
        .build)
    )
  }
}