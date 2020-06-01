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

/**
 * Execute an HTTP exchange, returning a Try.
 */
class HttpExchangeSyncInterpreter(client: OkHttpClient) extends HttpExchange[Try] {
  override def invoke(request: Request): Try[Response] = Try {
    val outgoing = HttpExchange.preprocessRequest(request)
    client.newCall(outgoing).execute()
  }
}

/**
 * Execute an HTTP exchange, returning a Future.
 */
class HttpExchangeAsyncInterpreter(client: OkHttpClient)(implicit ec: ExecutionContext) extends HttpExchange[Future] {
  override def invoke(request: Request): Future[Response] = {
    val outgoing = HttpExchange.preprocessRequest(request)
    val call = client.newCall(outgoing)
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