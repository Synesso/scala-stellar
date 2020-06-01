package stellar.horizon

import okhttp3.{Headers, HttpUrl, Response}
import org.specs2.ScalaCheck
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.horizon.io.{FakeHttpOperations, FakeHttpOperationsAsync, FakeHttpOperationsSync}
import stellar.horizon.json.AccountDetails

import scala.concurrent.Future
import scala.util.{Success, Try}

class AccountOperationsSpec(implicit env: ExecutionEnv) extends Specification with ScalaCheck {
  import AccountDetails._

  private val baseUrl = HttpUrl.parse("http://localhost/")

  "account operation blocking interpreter" should {
    import FakeHttpOperationsSync.jsonResponse

    "fetch account details by account id" >> prop { accountDetail: AccountDetail =>
      val mockHttpExchange = new FakeHttpOperationsSync(
        mockInvoke = jsonResponse(asJsonDoc(accountDetail))
      )
      val horizon = Horizon.sync(
        baseUrl = baseUrl,
        createHttpExchange = _ => mockHttpExchange
      )

      horizon.accounts.accountDetail(accountDetail.id) must beEqualTo(Success(accountDetail))

      mockHttpExchange.calls.length mustEqual 1

      val request = mockHttpExchange.calls
        .headOption
        .collect {
          case FakeHttpOperations.Invoke(r) => r
        }
        .get

      request.url().toString mustEqual s"http://localhost/accounts/${accountDetail.id.encodeToString}"
    }
  }

  "account operation async interpreter" should {
    import FakeHttpOperationsAsync.jsonResponse

    "fetch account details by account id" >> prop { accountDetail: AccountDetail =>
      val mockHttpExchange = new FakeHttpOperationsAsync(
        mockInvoke = jsonResponse(asJsonDoc(accountDetail))
      )
      val horizon = Horizon.async(
        baseUrl = baseUrl,
        createHttpExchange = (_, _) => mockHttpExchange
      )

      horizon.accounts.accountDetail(accountDetail.id) must beEqualTo(accountDetail).await

      mockHttpExchange.calls.length mustEqual 1

      val request = mockHttpExchange.calls
        .headOption
        .collect {
          case FakeHttpOperations.Invoke(r) => r
        }
        .get

      request.url().toString mustEqual s"http://localhost/accounts/${accountDetail.id.encodeToString}"
    }
  }
}
