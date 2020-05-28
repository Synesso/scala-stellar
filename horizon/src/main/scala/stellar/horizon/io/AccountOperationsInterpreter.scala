package stellar.horizon.io

import java.io.IOException

import com.sun.deploy.net.HttpRequest
import okhttp3._
import org.json4s.native.JsonMethods
import stellar.horizon.AccountDetail
import stellar.horizon.io.MockHttpExchange.Invoke
import stellar.protocol.AccountId

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

object AccountOperationsInterpreter{

  def httpExchange(client: OkHttpClient, request: Request): Future[Response] = {
    val call = client.newCall(request)
    val promise = Promise[Response]()
    val callback = new Callback {
      // $COVERAGE-OFF$
      // TODO (jem) - Cannot cover with tests whilst the url is hard-coded.
      override def onFailure(call: Call, e: IOException): Unit = {
         promise.failure(e)
      }
      // $COVERAGE-ON$
      override def onResponse(call: Call, response: Response): Unit = {
        promise.success(response)
      }
    }
    call.enqueue(callback)
    promise.future
  }
}

trait HttpExchange[F[_]] {
  def invoke(request: Request): F[Response]
}

class HttpExchangeAsync extends HttpExchange[Future] {
  override def invoke(httpRequest: Request): Future[Response] = ???
}

object MockHttpExchange {
  case class Invoke(request: Request) extends Call
  sealed trait Call
}

class MockHttpExchange(
  mockInvoke: Invoke => Future[Response]
) extends HttpExchange[Future] {
  var calls: Seq[Call] = Seq.empty

  override def invoke(httpRequest: Request): Future[Response] = {
    val call = Invoke(httpRequest)
    calls :+= call
    mockInvoke(call)
  }

}

class AccountOperationsInterpreter(
  baseUri: String,
  httpExchange: HttpExchange[Future]
)(implicit ec: ExecutionContext) extends AccountOperations[Future] {
  override def accountDetail(accountId: AccountId): Future[AccountDetail] = {
    val request = _

    for {
      response  <- httpExchange.invoke(request)
      accountDetails <- Future {
        //deserialize json from response
      }
    } yield accountDetails
  }
}
