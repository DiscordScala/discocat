package org.discordscala.discocat

import cats.effect._
import cats.implicits._
import fs2._
import org.discordscala.discocat.ws.event.{MessageCreate, Ready, ReadyData}
import scala.io.StdIn

object Test extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    program[IO].as(ExitCode.Success)
  }

  def program[F[_]: ConcurrentEffect: ContextShift: Timer]: F[Unit] = {
    for {
      t <- Sync[F].delay(StdIn.readLine("Token? "))
      c <- Client[F](t)
      l <- c
        .login(
          EventHandler[F] {
            case MessageCreate(_, m) =>
              Stream
                .eval(
                  Sync[F].delay(println(s"Message $m"))
                )
            case Ready(_, ReadyData(_, user, _, _)) =>
              Stream.eval(
                Sync[F].delay(println(show"Ready! Logged in as $user."))
              )
          }
            :: Defaults.defaultEventHandler[F]
            :: Nil
        )
        .compile
        .drain
    } yield l
  }

}
