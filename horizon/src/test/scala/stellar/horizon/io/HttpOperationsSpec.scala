package stellar.horizon.io

import okhttp3._
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.BuildInfo
import stellar.horizon.io.HttpOperations.ServerError

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Success, Try}

class HttpOperationsSpec(implicit ec: ExecutionEnv) extends Specification {

  private val request = new Request.Builder().url(HttpUrl.parse("https://horizon.stellar.org/")).build
  private val response = new Response.Builder()
    .request(request)
    .code(200)
    .message("OK")
    .protocol(Protocol.HTTP_2)
    .build()

  "blocking exchange" should {
    def fakeExchange(input: Request, resp: Response): Try[Response] = {
      input.url().toString mustEqual "https://horizon.stellar.org/"
      input.header("X-Client-Name") mustEqual BuildInfo.name
      input.header("X-Client-Version") mustEqual BuildInfo.version

      Success(resp)
    }

    "execute request/response" >> {
      new HttpOperationsSyncInterpreter(fakeExchange(_, response)).invoke(request) must beSuccessfulTry(response)
    }

    "handle server errors" >> {
      val serverErrorResponse = response.newBuilder().code(500).message("broken").build()
      new HttpOperationsSyncInterpreter(fakeExchange(_, serverErrorResponse))
        .handle(serverErrorResponse, Try("ok")) must beFailedTry[String].like { case ServerError(message) =>
          message mustEqual "broken"
        }
    }
  }

  "async exchange" should {
    def fakeExchange(input: Request, resp: Response): Future[Response] = {
      input.url().toString mustEqual "https://horizon.stellar.org/"
      input.header("X-Client-Name") mustEqual BuildInfo.name
      input.header("X-Client-Version") mustEqual BuildInfo.version

      Future.successful(resp)
    }

    "execute request/response" >> {
      new HttpOperationsAsyncInterpreter(fakeExchange(_, response)).invoke(request) must beEqualTo(response).await
    }

    "capture failures" >> {
      val error = new RuntimeException
      def fakeExchange(input: Request): Future[Response] = {
        Future.failed(error)
      }
      new HttpOperationsAsyncInterpreter(fakeExchange).invoke(request) must throwA[Throwable](error).await
    }

    "handle server errors" >> {
      val serverErrorResponse = response.newBuilder().code(500).message("broken").build()
      new HttpOperationsAsyncInterpreter(fakeExchange(_, serverErrorResponse))
        .handle(serverErrorResponse, Future("ok")) must throwA[ServerError].like { case ServerError(message) =>
        message mustEqual "broken"
      }.await
    }
  }
}
