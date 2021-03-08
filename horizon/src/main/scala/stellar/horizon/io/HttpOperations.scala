package stellar.horizon.io

import okhttp3._
import stellar.BuildInfo
import stellar.horizon.io.HttpOperations.{BadRequest, NotFound, ServerError}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

object HttpOperations {

  private[horizon] def preprocessRequest(request: Request): Request = {
    new Request.Builder(request)
      .addHeader("X-Client-Name", BuildInfo.name)
      .addHeader("X-Client-Version", BuildInfo.version)
      .build()
  }

  case class BadRequest(body: String) extends Exception(body)
  case class NotFound(body: String) extends Exception(body)
  case class ServerError(message: String) extends Exception(message)
}

/**
 * Execute an HTTP exchange with the declared effect type.
 */
trait HttpOperations[F[_]] {
  def invoke(request: Request): F[Response]
  def handle[T](
    response: Response,
    ok: => F[T]
  ): F[T] = response.code() match {
    case 200 | 400 => ok
    case 404 => err(NotFound(response.body().string()))
    case 500 => err(ServerError(response.message()))
  }
  def err[T](e: Exception): F[T]
}

object HttpOperationsSyncInterpreter {
  def exchange(client: OkHttpClient, request: Request): Try[Response] = Try {
    client.newCall(request).execute()
  }
}

/**
 * Execute an HTTP exchange, returning a Try.
 */
class HttpOperationsSyncInterpreter(exchange: Request => Try[Response]) extends HttpOperations[Try] {
  override def invoke(request: Request): Try[Response] = {
    val outgoing = HttpOperations.preprocessRequest(request)
    exchange(outgoing)
  }

  override def err[T](e: Exception): Try[T] = Failure(e)
}

object HttpOperationsAsyncInterpreter {
  def exchange(client: OkHttpClient, request: Request)(implicit ec: ExecutionContext): Future[Response] = {
    Future(client.newCall(request).execute())
  }
}

/**
 * Execute an HTTP exchange, returning a Future.
 */
class HttpOperationsAsyncInterpreter(exchange: Request => Future[Response]) extends HttpOperations[Future] {
  override def invoke(request: Request): Future[Response] = {
    val outgoing = HttpOperations.preprocessRequest(request)
    exchange(outgoing)
  }

  override def err[T](e: Exception): Future[T] = Future.failed(e)
}