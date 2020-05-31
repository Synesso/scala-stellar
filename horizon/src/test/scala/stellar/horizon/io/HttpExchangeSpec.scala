package stellar.horizon.io

import java.net.UnknownHostException

import okhttp3.{HttpUrl, Request, Response}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification

import scala.concurrent.duration._

class HttpExchangeSpec(implicit ec: ExecutionEnv) extends Specification {

  private val request = new Request.Builder().url(HttpUrl.parse("https://horizon.stellar.org/")).build

  "blocking exchange" should {
    "execute request/response" >> {
      HttpExchangeSync.invoke(request) must beSuccessfulTry.like { case response: Response =>
        response.body().string() must contain("Public Global Stellar Network ; September 2015")
      }
    }
  }

  "async exchange" should {
    "execute request/response" >> {
      HttpExchangeAsync.invoke(request) must beLike[Response] { case response: Response =>
        response.body().string() must contain("Public Global Stellar Network ; September 2015")
      }.await(0, 10.seconds)
    }

    "capture failures" >> {
      val badRequest = new Request.Builder().url("http://localtoast:42/").build
      HttpExchangeAsync.invoke(badRequest) must throwAn[UnknownHostException].await
    }
  }

}
