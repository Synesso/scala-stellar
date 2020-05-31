package stellar.horizon

import okhttp3.{HttpUrl, Request, Response}
import stellar.BuildInfo
import stellar.horizon.io.HttpExchange

/**
 * A Horizon instance and how to access it.
 * @param url the base URL of the instance
 * @param http the request/response exchange
 * @tparam F the effect responses will be wrapped in
 */
case class Horizon[F[_]](
  url: HttpUrl,
  http: HttpExchange[F]
) {

  def get(path: String, params: Map[String, String] = Map.empty): F[Response] = http.invoke(request(path, params))

  private def request(path: String, params: Map[String, String] = Map.empty): Request = {
    val requestUrl = params.foldLeft(url.newBuilder().addPathSegments(path)) { case (builder, (key, value)) =>
      builder.addQueryParameter(key, value)
    }.build()
    new Request.Builder()
      .url(requestUrl)
      .addHeader("X-Client-Name", BuildInfo.name)
      .addHeader("X-Client-Version", BuildInfo.version)
      .build()
  }
}
