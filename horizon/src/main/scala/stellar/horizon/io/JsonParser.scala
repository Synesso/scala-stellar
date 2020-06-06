package stellar.horizon.io

import okhttp3.Response
import org.json4s.Formats
import org.json4s.native.JsonMethods.{parse => json4sParse}

import scala.util.{Failure, Success, Try}

trait JsonParser {

  def parse[T](response: Response)(implicit formats: Formats, manifest: Manifest[T]): Either[JsonParsingException, T] =
    parse(response.body().string())

  def parse[T](body: String)(implicit formats: Formats, manifest: Manifest[T]): Either[JsonParsingException, T] =
    toEither(Try(json4sParse(body).extract[T]), body)

  private def toEither[T](tried: Try[T], body: String): Either[JsonParsingException, T] = tried match {
    case Success(value) => Right(value)
    case Failure(exception) => Left(JsonParsingException(exception, body))
  }
}

case class JsonParsingException(cause: Throwable, body: String) extends Exception(cause)