package stellar.horizon.io

import okhttp3.{HttpUrl, Request, Response}
import org.json4s.{DefaultFormats, Formats}
import stellar.horizon.HorizonState
import stellar.horizon.json.HorizonStateReader

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object MetaOperations extends JsonParser {

  def stateRequest(horizonBaseUrl: HttpUrl): Request =
    new Request.Builder().url(horizonBaseUrl).build()

  def responseToHorizonState(response: Response): Either[JsonParsingException, HorizonState] = {
    implicit val formats: Formats = DefaultFormats + HorizonStateReader
    parse[HorizonState](response)
  }
}

/**
 * Operations related to Horizon server information.
 * @tparam F the effect type.
 */
trait MetaOperations[F[_]] {
  def state: F[HorizonState]
}

/**
 * Horizon meta operations effected by Scala Try.
 */
class MetaOperationsSyncInterpreter(
  horizonBaseUrl: HttpUrl,
  httpExchange: HttpOperations[Try]
) extends MetaOperations[Try] {

  override def state: Try[HorizonState] = {
    val request = MetaOperations.stateRequest(horizonBaseUrl)
    for {
      response <- httpExchange.invoke(request)
      result <- httpExchange.handle(response,
        MetaOperations.responseToHorizonState(response).toTry
      )
    } yield result
  }
}

/**
 * Horizon meta operations effected by Scala Future.
 */
class MetaOperationsAsyncInterpreter(
  horizonBaseUrl: HttpUrl,
  httpExchange: HttpOperations[Future]
)(implicit ec: ExecutionContext) extends MetaOperations[Future] {

  override def state: Future[HorizonState] = {
    val request = MetaOperations.stateRequest(horizonBaseUrl)
    for {
      response <- httpExchange.invoke(request)
      result <- httpExchange.handle(response,
        Future(MetaOperations.responseToHorizonState(response).toTry.get)
      )
    } yield result
  }
}
