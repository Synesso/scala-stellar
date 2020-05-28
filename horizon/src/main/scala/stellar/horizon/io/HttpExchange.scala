package stellar.horizon.io

import java.io.IOException

import okhttp3._
import stellar.BuildInfo

import scala.concurrent.{Future, Promise}
import scala.util.Try

/**
 * Execute an HTTP exchange with the declared effect type.
 */
trait HttpExchange[F[_]] {
  def invoke(request: Request): F[Response]
}

/**
 * Execute an HTTP exchange, returning a Try.
 */
class HttpExchangeSync extends HttpExchange[Try] {
  override def invoke(request: Request): Try[Response] = Try(HttpExchange.invokeSync(request))
}

/**
 * Execute an HTTP exchange, returning a Future.
 */
class HttpExchangeAsync extends HttpExchange[Future] {
  override def invoke(request: Request): Future[Response] = HttpExchange.invokeAsync(request)
}

object HttpExchange {
  private val client: OkHttpClient = new OkHttpClient()

  def invokeSync(request: Request): Response = client.newCall(addSdkHeaders(request)).execute()

  def invokeAsync(request: Request): Future[Response] = {
    val call = client.newCall(addSdkHeaders(request))
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

  private def addSdkHeaders(request: Request): Request = {
    request.newBuilder()
      .addHeader("X-Client-Name", BuildInfo.name)
      .addHeader("X-Client-Version", BuildInfo.version)
      .build()
  }
}