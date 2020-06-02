package stellar.horizon.io

import java.io.IOException

import okhttp3._
import stellar.BuildInfo

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

object HttpOperations {

  private[horizon] def preprocessRequest(request: Request): Request = {
    new Request.Builder(request)
      .addHeader("X-Client-Name", BuildInfo.name)
      .addHeader("X-Client-Version", BuildInfo.version)
      .build()
  }

  case class NotFound(message: String) extends Exception(message)
}

/**
 * Execute an HTTP exchange with the declared effect type.
 */
trait HttpOperations[F[_]] {
  def invoke(request: Request): F[Response]
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
}