package org.discordscala.discocat

import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.Executors

import cats._
import cats.implicits._
import cats.effect._
import cats.effect.concurrent.{Deferred, Ref}
import fs2._
import org.discordscala.discocat.ws.{Event, Socket}
import spinoco.fs2.http
import spire.math.ULong

import scala.io.StdIn

object Test extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    implicit val ag: AsynchronousChannelGroup = AsynchronousChannelGroup.withThreadPool(Executors.newCachedThreadPool())
    program[IO].as(ExitCode.Success)
  }

  def program[F[_]: ConcurrentEffect: ContextShift: Timer](implicit ag: AsynchronousChannelGroup): F[Unit] = {
    for {
      h <- http.client[F]()
      d <- Deferred[F, Socket[F]]
      t <- Sync[F].delay(StdIn.readLine("Token? "))
      c = Client[F](t, h, d)
      l <- c.login(
        ((_: Ref[F, Option[ULong]]) => (in: Stream[F, Event[F]]) => in.evalMap(e => Sync[F].delay(println(e))))
          :: defaultEventHandler[F]
          :: Nil
      )
    } yield l
  }

}
