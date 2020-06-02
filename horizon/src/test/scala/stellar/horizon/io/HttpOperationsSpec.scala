package stellar.horizon.io

import okhttp3._
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.BuildInfo

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
    "execute request/response" >> {
      def fakeExchange(input: Request): Try[Response] = {
        input.url().toString mustEqual "https://horizon.stellar.org/"
        input.header("X-Client-Name") mustEqual BuildInfo.name
        input.header("X-Client-Version") mustEqual BuildInfo.version

        Success(response)
      }

      new HttpOperationsSyncInterpreter(fakeExchange).invoke(request) must beSuccessfulTry(response)
    }
  }

  "async exchange" should {
    "execute request/response" >> {
      def fakeExchange(input: Request): Future[Response] = {
        input.url().toString mustEqual "https://horizon.stellar.org/"
        input.header("X-Client-Name") mustEqual BuildInfo.name
        input.header("X-Client-Version") mustEqual BuildInfo.version

        Future.successful(response)
      }

      new HttpOperationsAsyncInterpreter(fakeExchange).invoke(request) must beEqualTo(response)
        .await(0, 10.seconds)
    }

    "capture failures" >> {
      val error = new RuntimeException
      def fakeExchange(input: Request): Future[Response] = {
        Future.failed(error)
      }
      new HttpOperationsAsyncInterpreter(fakeExchange).invoke(request) must throwA[Throwable](error)
        .await(0, 10.seconds)
    }
  }
}
