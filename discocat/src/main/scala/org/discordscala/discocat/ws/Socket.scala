package org.discordscala.discocat

package ws

import cats._
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import fs2.concurrent.{Queue, Topic}
import fs2.{io => _, _}
import io.circe.Json
import io.circe.fs2._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.client.jdkhttpclient.WSFrame
import spire.math.ULong

case class Socket[F[_]](
  client: Client[F],
  handlers: EventHandlers[F],
  inbound: Topic[F, Event[F]],
  outbound: Queue[F, Event[F]],
  sequence: Ref[F, Option[ULong]],
  maxQueued: Int = 256,
) {

  def handledEffects(implicit concurrent: Concurrent[F]): Stream[F, Unit] =
    Stream(handlers.map(_(sequence)(inbound.subscribe(maxQueued))): _*).parJoinUnbounded

  def send[A](e: Event.Aux[F, A]): F[Unit] = outbound.enqueue1(e)

  def pipe(implicit concurrent: Concurrent[F]): Pipe[F, String, WSFrame.Text] = in => {
    outbound.dequeue.through(frameify).concurrently(in.through(handle).merge(handledEffects))
  }

  def frameify(implicit flatMap: FlatMap[F]): Pipe[F, Event[F], WSFrame.Text] =
    _.map(Event.encoder[F](_))
      .evalMap(
        j =>
          sequence.get.map {
            case Some(seq) => j.mapObject(_.add("s", seq.asJson))
            case None => j
        }
      )
      .map(_.noSpaces)
      .map(WSFrame.Text(_))

  def handle(implicit r: ApplicativeError[F, Throwable], c: Concurrent[F]): Pipe[F, String, Unit] =
    _.through(stringStreamParser[F])
      .evalTap(updateSequence)
      .through(decoder[F, EventStruct])
      .map(client.decode)
      .unNone
      .evalMap(r.fromEither)
      .through(inbound.publish)

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
