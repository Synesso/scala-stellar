package stellar.horizon.io

import java.io.IOException

import okhttp3._

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
object HttpExchangeSync extends HttpExchange[Try] {
  private val client: OkHttpClient = new OkHttpClient()
  override def invoke(request: Request): Try[Response] = Try(client.newCall(request).execute())
}

/**
 * Execute an HTTP exchange, returning a Future.
 */
object HttpExchangeAsync extends HttpExchange[Future] {
  private val client: OkHttpClient = new OkHttpClient()
  override def invoke(request: Request): Future[Response] = {
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