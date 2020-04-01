package org.discordscala.discocat

import cats.effect._
import cats.effect.concurrent.{Deferred, Ref}
import cats.implicits._
import fs2.{io => _, _}
import fs2.concurrent.{Queue, Topic}
import io.circe.DecodingFailure
import org.discordscala.discocat.util.RequestUtil
import org.discordscala.discocat.ws.{Event, EventDecoder, EventStruct, Socket}
import org.http4s._
import org.http4s.client.{Client => HttpClient}
import org.http4s.client.jdkhttpclient._
import org.http4s.implicits._
import spire.math.ULong

case class Client[F[_]](
  token: String,
  httpClient: HttpClient[F],
  wsClient: WSClient[F],
  deferredSocket: Deferred[F, Socket[F]],
  decoder: EventDecoder = Defaults.defaultEventDecoder,
  apiRoot: Uri = uri"https://discordapp.com/api/v6/",
  gatewayRoot: Uri = uri"wss://gateway.discord.gg/?v=6&encoding=json",
) {

  val request: RequestUtil[F] = RequestUtil(this)

  def decode(e: EventStruct): Option[Either[DecodingFailure, Event[F]]] = decoder.decode(this).lift(e)

  def login(
    handlers: EventHandlers[F],
    topic: Topic[F, Event[F]],
    queue: Queue[F, Event[F]],
    ref: Ref[F, Option[ULong]]
  )(implicit concurrent: Concurrent[F]): Stream[F, Unit] =
    for {
      conn <- Stream.resource(wsClient.connectHighLevel(WSRequest(gatewayRoot)))
      sock = Socket(this, handlers, topic, queue, ref)
      _ <- Stream.eval(deferredSocket.complete(sock))
      handle <- conn.receiveStream
        .collect {
          case WSFrame.Text(data, _) => data
        }
        .through(sock.pipe)
        .through(conn.sendPipe)
    } yield handle

  def login(handlers: EventHandlers[F])(implicit concurrent: Concurrent[F]): Stream[F, Unit] =
    for {
      inbound <- Stream.eval(Defaults.eventTopic[F](this))
      outbound <- Stream.eval(Defaults.eventQueue[F])
      ref <- Stream.eval(Defaults.sequenceRef[F])
      l <- login(handlers, inbound, outbound, ref)
    } yield l

}

object Client {

  def apply[F[_]: ConcurrentEffect: ContextShift: Timer](
    token: String
  ): F[Client[F]] =
    for {
      (h, ws) <- Defaults.httpClient[F]
      d <- Defaults.socketDeferred
      c = Client(token, h, ws, d)
    } yield c

}
