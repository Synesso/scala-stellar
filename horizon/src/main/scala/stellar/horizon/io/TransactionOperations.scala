package stellar.horizon.io

import okhttp3.Response
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats}
import stellar.horizon.TransactionResponse
import stellar.horizon.json.TransactionResponseReader

object TransactionOperations {
  def responseToTransactionResponse(response: Response): TransactionResponse = {
    implicit val formats: Formats = DefaultFormats + TransactionResponseReader
    parse(response.body().string()).extract[TransactionResponse]
  }

}
