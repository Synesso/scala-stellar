package stellar.horizon.json

import org.json4s.native.JsonMethods.{pretty, render}
import org.json4s.{CustomSerializer, DefaultFormats, Formats, JObject}

import scala.util.control.NonFatal

class JsReader[T](f: JObject => T)(implicit m: Manifest[T]) extends CustomSerializer[T](_ => ({
  case o: JObject =>
    try {
      f(o)
    } catch {
      case NonFatal(t) => throw ResponseParseException(pretty(render(o)), t)
    }
}, PartialFunction.empty))

object JsReader {
  implicit val formats: Formats = DefaultFormats

  def doubleStringToLong(key: String, o: JObject): Long = optDoubleStringToLong(key, o).get

  def optDoubleStringToLong(key: String, o: JObject): Option[Long] =
    (o \ key).extractOpt[String].map(value =>
      (BigDecimal(value) * BigDecimal(10).pow(7)).toLongExact)
}


case class ResponseParseException(doc: String, cause: Throwable)
  extends Exception(s"Unable to parse document:\n$doc", cause)

