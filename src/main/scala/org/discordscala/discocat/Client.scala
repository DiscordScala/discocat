package org.discordscala.discocat

import cats._
import cats.effect._
import cats.effect.concurrent.{Deferred, Ref}
import cats.implicits._
import fs2.concurrent.{Queue, Topic}
import fs2.{io => _, _}
import io.circe.DecodingFailure
import java.nio.channels.AsynchronousChannelGroup
import org.discordscala.discocat.ws.event.HeartbeatAck
import org.discordscala.discocat.ws.{Event, EventDecoder, EventStruct, Socket}
import spinoco.fs2.http.HttpClient
import spinoco.fs2.http.websocket.WebSocketRequest
import spinoco.protocol.http.Uri
import spire.math.ULong

case class Client[F[_]](
  token: String,
  httpClient: HttpClient[F],
  deferredSocket: Deferred[F, Socket[F]],
  decoder: EventDecoder = Defaults.defaultEventDecoder,
  apiRoot: Uri = Uri.https("discordapp.com", "/api/v6/"),
  gatewayRoot: Uri = Uri.wss("gateway.discord.gg", "/?v=6&encoding=json"),
) {

  def decode(e: EventStruct): Option[Either[DecodingFailure, Event[F]]] = decoder.decode(this).lift(e)

  def login(handlers: EventHandlers[F], topic: Topic[F, Event[F]], queue: Queue[F, Event[F]], ref: Ref[F, Option[ULong]])(implicit concurrent: Concurrent[F]): F[Unit] =
    (for {
      client <- Stream(httpClient)
      req = WebSocketRequest.wss(
        gatewayRoot.host.host,
        gatewayRoot.host.port.getOrElse(443),
        gatewayRoot.path.stringify,
        gatewayRoot.query.params: _*
      )
      sock = Socket(this, handlers, topic, queue, ref)
      _ <- Stream.eval(deferredSocket.complete(sock))
      o <- client.websocket(req, sock.pipe)(scodec.codecs.utf8, scodec.codecs.utf8)
    } yield o).compile.drain

  def login(handlers: EventHandlers[F])(implicit concurrent: Concurrent[F]): F[Unit] = for {
      inbound <- Defaults.eventTopic[F](this)
      outbound <- Defaults.eventQueue[F]
      ref <- Defaults.sequenceRef[F]
      l <- login(handlers, inbound, outbound, ref)
    } yield l

}

object Client {

  def apply[F[_]: ConcurrentEffect: ContextShift: Timer](token: String)(implicit ag: AsynchronousChannelGroup): F[Client[F]] = for {
      h <- Defaults.httpClient[F]
      d <- Defaults.socketDeferred
      c = Client(token, h, d)
    } yield c

}
