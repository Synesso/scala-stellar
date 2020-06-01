package stellar.horizon.io

import java.io.IOException

import okhttp3._
import stellar.BuildInfo

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

object HttpExchange {

  private[horizon] def preprocessRequest(request: Request): Request = {
    new Request.Builder(request)
      .addHeader("X-Client-Name", BuildInfo.name)
      .addHeader("X-Client-Version", BuildInfo.version)
      .build()
  }
}

/**
 * Execute an HTTP exchange with the declared effect type.
 */
trait HttpExchange[F[_]] {
  def invoke(request: Request): F[Response]
}

object HttpExchangeSyncInterpreter {
  def exchange(client: OkHttpClient, request: Request): Try[Response] = Try {
    client.newCall(request).execute()
  }
}

/**
 * Execute an HTTP exchange, returning a Try.
 */
class HttpExchangeSyncInterpreter(exchange: Request => Try[Response]) extends HttpExchange[Try] {
  override def invoke(request: Request): Try[Response] = {
    val outgoing = HttpExchange.preprocessRequest(request)
    exchange(outgoing)
  }
}

object HttpExchangeAsyncInterpreter {
  def exchange(client: OkHttpClient, request: Request): Future[Response] = {
    val call = client.newCall(request)
    val promise = Promise[Response]()
    val callback = new Callback {
      override def onFailure(call: Call, e: IOException): Unit = {
        promise.failure(e)
      }

      override def onResponse(call: Call, response: Response): Unit = {
        promise.success(response)
      }
    }
    call.enqueue(callback)
    promise.future
  }

}

/**
 * Execute an HTTP exchange, returning a Future.
 */
class HttpExchangeAsyncInterpreter(exchange: Request => Future[Response])(implicit ec: ExecutionContext) extends HttpExchange[Future] {
  override def invoke(request: Request): Future[Response] = {
    val outgoing = HttpExchange.preprocessRequest(request)
    exchange(outgoing)
  }
}