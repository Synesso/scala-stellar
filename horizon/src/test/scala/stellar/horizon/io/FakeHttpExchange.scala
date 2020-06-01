package stellar.horizon.io

import okhttp3._
import stellar.horizon.io.FakeHttpExchange.{Invoke, MediaTypes}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

object FakeHttpExchange {
  case class Invoke(request: Request) extends Call
  sealed trait Call

  object MediaTypes {
    val json = MediaType.get("application/json; charset=utf-8")
  }
}

trait FakeHttpExchange[F[_]] extends HttpExchange[F] {
  def calls: Seq[FakeHttpExchange.Call]
}

object FakeHttpExchangeSync {
  def jsonResponse(jsonText: String, statusCode: Int = 200, message: String = "OK"): Invoke => Try[Response] = invoke => Success(
    new Response.Builder()
      .protocol(Protocol.HTTP_2)
      .message(message)
      .code(statusCode)
      .request(invoke.request)
      .addHeader("Content-Type", "application/json")
      .body(ResponseBody.create(jsonText, MediaTypes.json))
      .build()
  )
}

class FakeHttpExchangeSync(
  mockInvoke: Invoke => Try[Response]
) extends FakeHttpExchange[Try] {
  var calls: Seq[FakeHttpExchange.Call] = List.empty

  override def invoke(request: Request): Try[Response] = {
    val call = Invoke(request)
    calls :+= call
    mockInvoke(call)
  }
}

object FakeHttpExchangeAsync {
  def jsonResponse(jsonText: String, statusCode: Int = 200, message: String = "OK"): Invoke => Future[Response] = invoke => Future.successful(
    new Response.Builder()
      .protocol(Protocol.HTTP_2)
      .message(message)
      .code(statusCode)
      .request(invoke.request)
      .addHeader("Content-Type", "application/json")
      .body(ResponseBody.create(jsonText, MediaTypes.json))
      .build()
  )
}

/**
 * For testing, declare the behaviour when invocations are made and record the requests.
 */
class FakeHttpExchangeAsync(
  mockInvoke: Invoke => Future[Response]
)(implicit ec: ExecutionContext) extends FakeHttpExchange[Future] {
  var calls: Seq[FakeHttpExchange.Call] = List.empty

  override def invoke(request: Request): Future[Response] = {
    val call = Invoke(request)
    calls :+= call
    mockInvoke(call)
  }
}
