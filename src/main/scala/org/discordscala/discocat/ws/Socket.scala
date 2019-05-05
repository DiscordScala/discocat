package org.discordscala.discocat

package ws

import cats._
import cats.implicits._
import cats.effect._
import cats.effect.concurrent.{Deferred, Ref}
import fs2.{io => _, _}
import fs2.concurrent.Queue
import io.circe.{Json, JsonObject}
import io.circe.fs2._
import io.circe.syntax._
import io.circe.generic.auto._
import spinoco.fs2.http.websocket.Frame
import spire.math.ULong

case class Socket[F[_]](
  client: Client[F],
  handlers: EventHandlers[F],
  queue: Queue[F, Event[F]],
  sequence: Ref[F, Option[ULong]]
) {

  val sequencedHandlers: List[Pipe[F, Event[F], Unit]] = handlers.map(_(sequence))

  def send[A](e: Event.Aux[F, A])(implicit fmap: FlatMap[F]): F[Unit] = queue.enqueue1(e)

  def pipe(implicit concurrent: Concurrent[F]): Pipe[F, Frame[String], Frame[String]] = in => {
    queue.dequeue.through(frameify).concurrently(in.through(handle))
  }

  def frameify(implicit flatMap: FlatMap[F]): Pipe[F, Event[F], Frame[String]] =
    _.map(Event.encoder[F](_))
      .evalMap(
        j =>
          sequence.get.map {
            case Some(seq) => j.mapObject(_.add("s", seq.asJson))
            case None => j
        }
      )
      .map(_.noSpaces)
      .map(Frame.Text(_))

  def handle(implicit r: ApplicativeError[F, Throwable], c: Concurrent[F]): Pipe[F, Frame[String], Unit] =
    _.map(_.a)
      .through(stringStreamParser[F])
      .evalTap(updateSequence)
      .evalTap(j => Sync[F].delay(println(s"Got $j")))
      .through(decoder[F, EventStruct])
      .attempt
      .evalTap(e => Sync[F].delay(println(s"Got2 $e")))
      .collect {
        case Right(value) => value
      }
      .evalMap(struct => r.fromEither(client.decode(struct)))
      .attempt
      .evalTap(e => Sync[F].delay(println(s"Got3 $e")))
      .collect {
        case Right(value) => value
      }
      .broadcastThrough(sequencedHandlers: _*)

  def updateSequence(j: Json)(implicit r: ApplicativeError[F, Throwable]): F[Unit] = {
    val optULong = for {
      obj <- j.asObject
      seq <- obj("s")
      num <- seq.asNumber
      bigInt <- num.toBigInt
    } yield ULong.fromBigInt(bigInt)
    optULong match {
      case Some(s) => sequence.set(Some(s))
      case None => r.pure(())
    }
  }

}
