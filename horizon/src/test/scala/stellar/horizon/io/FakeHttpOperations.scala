package stellar.horizon.io

import okhttp3._
import stellar.horizon.io.FakeHttpOperations.{Invoke, MediaTypes}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object FakeHttpOperations {
  case class Invoke(request: Request) extends Call
  sealed trait Call

  object MediaTypes {
    val json = MediaType.get("application/json; charset=utf-8")
  }
}

trait FakeHttpOperations[F[_]] extends HttpOperations[F] {
  def calls: Seq[FakeHttpOperations.Call]
}

object FakeHttpOperationsSync {
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

class FakeHttpOperationsSync(
  fakeInvoke: Invoke => Try[Response]
) extends FakeHttpOperations[Try] {
  var calls: Seq[FakeHttpOperations.Call] = List.empty

  override def invoke(request: Request): Try[Response] = {
    val call = Invoke(request)
    calls :+= call
    fakeInvoke(call)
  }

  override def err[T](e: Exception): Try[T] = Failure(e)
}

object FakeHttpOperationsAsync {
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
class FakeHttpOperationsAsync(
  fakeInvoke: Invoke => Future[Response]
)(implicit ec: ExecutionContext) extends FakeHttpOperations[Future] {
  var calls: Seq[FakeHttpOperations.Call] = List.empty

  override def invoke(request: Request): Future[Response] = {
    val call = Invoke(request)
    calls :+= call
    fakeInvoke(call)
  }

  override def err[T](e: Exception): Future[T] = Future.failed(e)
}
