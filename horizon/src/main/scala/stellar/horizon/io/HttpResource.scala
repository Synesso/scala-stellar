package stellar.horizon.io

import java.io.IOException

import okhttp3.{Call, Callback, OkHttpClient, Request, Response}
import org.json4s.{DefaultFormats, Formats}
import org.json4s.native.JsonMethods
import stellar.horizon.{AccountDetail, AccountDetailReader}
import stellar.protocol.AccountId

import scala.concurrent.{Future, Promise}
import scala.util.Try

class HttpResource {
  val client = new OkHttpClient()

  implicit val formats: Formats = DefaultFormats + AccountDetailReader

  // TODO (jem) - Wrapper type for
  //  Caused by: stellar.horizon.json.ResponseParseException: Unable to parse document:
  //  {
  //    "type":"https://stellar.org/horizon-errors/not_found",
  //    "title":"Resource Missing",
  //    "status":404,
  //    "detail":"The resource at the url requested was not found.  This usually occurs for one of two reasons:  The url requested is not valid, or no data in our database could be found with the parameters provided."
  //  }
  def accountDetail(accountId: AccountId): Future[AccountDetail] = {
    val call = client.newCall(new Request.Builder().url(s"https://horizon-testnet.stellar.org/accounts/${accountId.encodeToString}").build())
    val promise = Promise[AccountDetail]()
    val callback = new Callback {
      // $COVERAGE-OFF$
      // TODO (jem) - Cannot cover with tests whilst the url is hard-coded.
      override def onFailure(call: Call, e: IOException): Unit = {
         promise.failure(e)
      }
      // $COVERAGE-ON$
      override def onResponse(call: Call, response: Response): Unit = {
        promise.complete(Try(JsonMethods.parse(response.body().string()).extract[AccountDetail]))
      }
    }
    call.enqueue(callback)
    promise.future
  }
}
