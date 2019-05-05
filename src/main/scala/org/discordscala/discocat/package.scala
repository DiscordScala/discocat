package org.discordscala

import java.util.concurrent.TimeUnit

import cats.{FlatMap, Functor}
import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import fs2.{io => _, _}
import io.circe.Decoder.Result
import io.circe.{Decoder, DecodingFailure, HCursor}
import io.circe.generic.auto._
import io.circe.generic.extras._
import io.circe.syntax._
import org.discordscala.discocat.ws.event.{Heartbeat, HeartbeatAck, Hello, HelloData, Identify, IdentifyData, IdentifyProperties}
import org.discordscala.discocat.ws.{Event, EventDecoder, EventStruct}
import scodec.Encoder
import spire.math.ULong

import scala.concurrent.duration.FiniteDuration

package object discocat {

  type EventHandler[F[_]] = Ref[F, Option[ULong]] => Pipe[F, Event[F], Unit]
  type EventHandlers[F[_]] = List[EventHandler[F]]

  val defaultEventDecoder: EventDecoder = new EventDecoder {
    override def decode[F[_]](client: Client[F], struct: EventStruct): Either[DecodingFailure, Event[F]] = {
      struct match {
        case EventStruct(10, None, d) =>
          d.as[HelloData].map(Hello(client, _))
        case EventStruct(11, None, _) =>
          Right(HeartbeatAck(client))
      }
    }
  }

  def defaultEventHandler[F[_]: Timer: Concurrent]: EventHandler[F] =
    sequenceRef =>
      _.flatMap {
        case Hello(cli, HelloData(interval)) =>
          val beat: Stream[F, Unit] = for {
            time <- Stream[F, FiniteDuration](FiniteDuration.apply(0, TimeUnit.MILLISECONDS)) ++ Stream.awakeEvery[F](
              FiniteDuration(interval.signed, TimeUnit.MILLISECONDS)
            )
            sock <- Stream.eval(cli.deferredSocket.get)
            seq <- Stream.eval(sock.sequence.get)
            heartbeat = Heartbeat(cli, seq)
            _ <- Stream.eval(Sync[F].delay(println(s"Heartbeating at $time")))
            sent <- Stream.eval(sock.send(heartbeat))
          } yield sent
          val identify: Stream[F, Unit] = for {
            sock <- Stream.eval(cli.deferredSocket.get)
            identify = Identify(cli, IdentifyData(cli.token))
            sent <- Stream.eval(sock.send(identify))
          } yield sent
          identify ++ beat
    }

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val ulongDecoder: Decoder[ULong] = (c: HCursor) =>
    c.value.asNumber
      .flatMap(_.toBigInt)
      .map(ULong.fromBigInt)
      .toRight(DecodingFailure("Attempt to decode non-integer as ULong", List()))

}
