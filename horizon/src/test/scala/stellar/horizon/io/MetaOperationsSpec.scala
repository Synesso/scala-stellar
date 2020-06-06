package stellar.horizon.io

import okhttp3.HttpUrl
import org.json4s.MappingException
import org.specs2.ScalaCheck
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.horizon.json.HorizonStates
import stellar.horizon.{Horizon, HorizonState}

import scala.util.Success

class MetaOperationsSpec(implicit env: ExecutionEnv) extends Specification with ScalaCheck {
  import HorizonStates._

  private val baseUrl = HttpUrl.parse("http://localhost/")

  "horizon meta blocking interpreter" should {
    import FakeHttpOperationsSync.jsonResponse

    "fetch server state" >> prop { state: HorizonState =>
      val fakeHttpExchange = new FakeHttpOperationsSync(fakeInvoke = jsonResponse(asJsonDoc(state)))

      val horizon = Horizon.sync(
        baseUrl = baseUrl,
        createHttpExchange = _ => fakeHttpExchange)

      horizon.meta.state must beEqualTo(Success(state))

      fakeHttpExchange.calls must beLike { case Seq(FakeHttpOperations.Invoke(r)) =>
        r.url().toString mustEqual s"http://localhost/"
      }
    }

    "handle unexpected document" >> {
      val document = """{"hello":"world"}"""
      val fakeHttpExchange = new FakeHttpOperationsSync(fakeInvoke = jsonResponse(document))

      val horizon = Horizon.sync(
        baseUrl = baseUrl,
        createHttpExchange = _ => fakeHttpExchange)

      horizon.meta.state must beFailedTry[HorizonState].like { case JsonParsingException(cause, body) =>
        cause must haveClass[MappingException]
        body mustEqual document
      }

      fakeHttpExchange.calls must beLike { case Seq(FakeHttpOperations.Invoke(r)) =>
        r.url().toString mustEqual s"http://localhost/"
      }
    }
  }

  "horizon meta async interpreter" should {
    import FakeHttpOperationsAsync.jsonResponse

    "fetch server state" >> prop { state: HorizonState =>
      val fakeHttpExchange = new FakeHttpOperationsAsync(fakeInvoke = jsonResponse(asJsonDoc(state)))

      val horizon = Horizon.async(
        baseUrl = baseUrl,
        createHttpExchange = (_, _) => fakeHttpExchange)

      horizon.meta.state must beEqualTo(state).await

      fakeHttpExchange.calls must beLike { case Seq(FakeHttpOperations.Invoke(r)) =>
        r.url().toString mustEqual s"http://localhost/"
      }
    }

    "handle unexpected document" >> {
      val document = """{"hello":"world"}"""
      val fakeHttpExchange = new FakeHttpOperationsAsync(fakeInvoke = jsonResponse(document))

      val horizon = Horizon.async(
        baseUrl = baseUrl,
        createHttpExchange = (_, _) => fakeHttpExchange)

      horizon.meta.state must throwA[JsonParsingException].like { case JsonParsingException(cause, body) =>
        cause must haveClass[MappingException]
        body mustEqual document
      }.await

      fakeHttpExchange.calls must beLike { case Seq(FakeHttpOperations.Invoke(r)) =>
        r.url().toString mustEqual s"http://localhost/"
      }
    }
  }
}
