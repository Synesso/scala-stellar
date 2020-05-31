package stellar.horizon

import okhttp3.HttpUrl
import org.specs2.ScalaCheck
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import stellar.horizon.io.FakeHttpExchange
import stellar.horizon.json.AccountDetails

import scala.concurrent.Future
import scala.util.Try

class AccountOperationsSpec(implicit env: ExecutionEnv) extends Specification with ScalaCheck {
  import AccountDetails._

  private val baseUrl = HttpUrl.parse("http://localhost/")

  "account operation blocking interpreter" should {
    "fetch account details by account id" >> prop { accountDetail: AccountDetail =>
      val exchange = FakeHttpExchange.respondOkJson(asJsonDoc(accountDetail), Try(_))
      val interpreter = new AccountOperationsBlockingInterpreter(Horizon(baseUrl, exchange))
      interpreter.accountDetail(accountDetail.id) must beSuccessfulTry(accountDetail)
      exchange.requests.map(_.url.toString) mustEqual List(s"http://localhost/accounts/${accountDetail.id.encodeToString}")
    }

    "fetch account details by account id" >> prop { accountDetail: AccountDetail =>
      val exchange = FakeHttpExchange.respondOkJson(asJsonDoc(accountDetail), Future(_))
      val interpreter = new AccountOperationsAsyncInterpreter(Horizon(baseUrl, exchange))
      interpreter.accountDetail(accountDetail.id) must beEqualTo(accountDetail).await
      exchange.requests.map(_.url.toString) mustEqual List(s"http://localhost/accounts/${accountDetail.id.encodeToString}")
    }
  }
}
