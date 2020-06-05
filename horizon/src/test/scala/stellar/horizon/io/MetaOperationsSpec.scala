package stellar.horizon.io

import okhttp3.HttpUrl
import org.specs2.ScalaCheck
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.horizon.io.HttpOperations.NotFound
import stellar.horizon.json.HorizonStates
import stellar.horizon.{AccountDetail, Horizon, HorizonState}
import stellar.protocol.AccountId

import scala.util.{Failure, Success}

class MetaOperationsSpec(implicit env: ExecutionEnv) extends Specification with ScalaCheck {
  import HorizonStates._

  private val baseUrl = HttpUrl.parse("http://localhost/")

  "horizon meta blocking interpreter" should {
    import FakeHttpOperationsSync.jsonResponse

    "fetch server state" >> prop { state: HorizonState =>
      val fakeHttpExchange = new FakeHttpOperationsSync(
        fakeInvoke = jsonResponse(asJsonDoc(state)))

      val horizon = Horizon.sync(
        baseUrl = baseUrl,
        createHttpExchange = _ => fakeHttpExchange)

      horizon.meta.state must beEqualTo(Success(state))

      fakeHttpExchange.calls must beLike { case Seq(FakeHttpOperations.Invoke(r)) =>
        r.url().toString mustEqual s"http://localhost/"
      }
    }

/*
    "handle missing account details" >> {
      val fakeHttpExchange = new FakeHttpOperationsSync(
        fakeInvoke = jsonResponse(accountDetailMissing, 404, "not found"))

      val horizon = Horizon.sync(
        baseUrl = baseUrl,
        createHttpExchange = _ => fakeHttpExchange)

      val accountId = AccountId.random
      horizon.account.detail(accountId) must beEqualTo(Failure(NotFound(s"account.detail(${accountId.encodeToString})")))

      fakeHttpExchange.calls must beLike { case Seq(FakeHttpOperations.Invoke(r)) =>
        r.url().toString mustEqual s"http://localhost/accounts/${accountId.encodeToString}"
      }
    }
*/
  }

/*
  "horizon meta async interpreter" should {
    import FakeHttpOperationsAsync.jsonResponse

    "fetch server state" >> prop { accountDetail: AccountDetail =>
      val fakeHttpExchange = new FakeHttpOperationsAsync(
        fakeInvoke = jsonResponse(asJsonDoc(accountDetail)))

      val horizon = Horizon.async(
        baseUrl = baseUrl,
        createHttpExchange = (_, _) => fakeHttpExchange)

      horizon.account.detail(accountDetail.id) must beEqualTo(accountDetail).await

      fakeHttpExchange.calls must beLike { case Seq(FakeHttpOperations.Invoke(r)) =>
        r.url().toString mustEqual s"http://localhost/accounts/${accountDetail.id.encodeToString}"
      }
    }

    "handle missing account details" >> {
      val fakeHttpExchange = new FakeHttpOperationsAsync(
        fakeInvoke = jsonResponse(accountDetailMissing, 404, "not found"))

      val horizon = Horizon.async(
        baseUrl = baseUrl,
        createHttpExchange = (_, _) => fakeHttpExchange)

      val accountId = AccountId.random
      horizon.account.detail(accountId) must
        throwA[NotFound](NotFound(s"account.detail(${accountId.encodeToString})")).await

      fakeHttpExchange.calls must beLike { case Seq(FakeHttpOperations.Invoke(r)) =>
        r.url().toString mustEqual s"http://localhost/accounts/${accountId.encodeToString}"
      }
    }
  }
*/
}
