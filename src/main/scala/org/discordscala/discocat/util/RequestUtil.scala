package org.discordscala.discocat.util

import cats.ApplicativeError
import fs2._
import org.discordscala.discocat.Client
import spinoco.fs2.http.body.BodyEncoder
import spinoco.fs2.http.{HttpRequest, HttpResponse}
import spinoco.protocol.http.HttpRequestHeader
import spinoco.protocol.http.header.value.HttpCredentials.{BasicHttpCredentials, DigestHttpCredentials, OAuthToken}
import spinoco.protocol.http.header.{Authorization, HttpHeader}

case class RequestUtil[F[_]](c: Client[F]) {

  val tokenHeader = Authorization(OAuthToken("Bot", c.token))

  def post[A](path: String, headers: List[HttpHeader], content: A)(implicit raiseThrowable: ApplicativeError[F, Throwable], bodyEncoder: BodyEncoder[A]): Stream[F, HttpResponse[F]] = {
    val thisReqPath = c.apiRoot.copy(path = c.apiRoot.path / path)
    val req = HttpRequest.post(thisReqPath, content)
    val hreq = headers match {
      case h :: t => req.appendHeader(h, t: _*)
      case Nil => req
    }
    val treq = hreq.appendHeader(tokenHeader)
    c.httpClient.request(treq)
  }

}
