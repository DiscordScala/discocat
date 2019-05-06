package org.discordscala.discocat

import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.{Executors, TimeUnit}

import cats._
import cats.implicits._
import cats.effect._
import cats.effect.concurrent.{Deferred, Ref}
import fs2._
import org.discordscala.discocat.model.Message
import org.discordscala.discocat.ws.event.MessageCreate
import org.discordscala.discocat.ws.{Event, Socket}
import spinoco.fs2.http
import spire.math.ULong

import scala.concurrent.duration.FiniteDuration
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
        ((_: Ref[F, Option[ULong]]) => (in: Stream[F, Event[F]]) => in.collect {
          case MessageCreate(_, Message(_, _, _, _, _, content)) => content
        }.evalMap(content => Sync[F].delay(println(content))))
          :: Defaults.defaultEventHandler[F]
          :: Nil
      )
    } yield l
  }

}
