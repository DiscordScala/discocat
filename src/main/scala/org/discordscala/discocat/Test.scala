package org.discordscala.discocat

import cats._
import cats.effect._
import cats.effect.concurrent.{Deferred, Ref}
import cats.implicits._
import fs2._
import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.Executors

import org.discordscala.discocat.instances._
import org.discordscala.discocat.model.Message
import org.discordscala.discocat.ws.event.{MessageCreate, Ready, ReadyData}
import org.discordscala.discocat.ws.{Event, Socket}

import scala.io.StdIn
import spinoco.fs2.http
import spire.math.ULong

object Test extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    implicit val ag: AsynchronousChannelGroup = AsynchronousChannelGroup.withThreadPool(Executors.newCachedThreadPool())
    program[IO].as(ExitCode.Success)
  }

  def program[F[_]: ConcurrentEffect: ContextShift: Timer](implicit ag: AsynchronousChannelGroup): F[Unit] = {
    for {
      t <- Sync[F].delay(StdIn.readLine("Token? "))
      c <- Client[F](t)
      l <- c.login(
        EventHandler[F] {
          case MessageCreate(_, m) =>
            Stream.eval(
              Sync[F].delay(println(show"Message by ${m.author} at ${m.timestamp} with mentions: ${m.mentions}"))
            ).flatTap { _ =>
              if(m.content == "!ping") {
                c.request.post(s"channels/${m.channelId}/messages", Nil, Map("content" -> "Pong!"))
              } else {
                Stream.empty
              }
            }
          case Ready(_, ReadyData(_, user, _, _)) =>
            Stream.eval(
              Sync[F].delay(println(show"Ready! Logged in as $user."))
            )
        }
          :: Defaults.defaultEventHandler[F]
          :: Nil
      ).compile.drain
    } yield l
  }

}
