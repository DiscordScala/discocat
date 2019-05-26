package org.discordscala

import cats.effect.concurrent.Ref
import fs2.{io => _, _}
import io.circe.generic.extras._
import io.circe.{CursorOp, Decoder, DecodingFailure, HCursor, Json}
import java.time.{Instant, ZoneId, ZonedDateTime}

import org.discordscala.discocat.model.{Member, MemberUser, PartialMember, User}
import org.discordscala.discocat.ws.Event

import scala.util.Try
import spire.math.ULong

package object discocat extends model.Implicits {

  type EventHandler[F[_]] = Ref[F, Option[ULong]] => Pipe[F, Event[F], Unit]
  type EventHandlers[F[_]] = List[EventHandler[F]]

  object EventHandler {

    def apply[F[_]](f: PartialFunction[Event[F], Stream[F, Unit]]): EventHandler[F] = _ => _.collect(f).flatten

  }

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val ulongDecoder: Decoder[ULong] = (c: HCursor) => {
    val numO = c.value.asNumber
    val strO = c.value.asString
    val numAsBI = numO.flatMap(_.toBigInt)
    val strAsBI = strO.flatMap(s => Try(BigInt(s)).toOption)
    val biO = numAsBI.map(Some(_)).getOrElse(strAsBI)
    biO
      .map(ULong.fromBigInt)
      .toRight(DecodingFailure(s"Attempt to decode non-integer as ULong: ${c.value}", List()))
  }

  implicit val timestampDecoder: Decoder[ZonedDateTime] = (c: HCursor) => {
    val strO = c.value.asString
    strO
      .map(ZonedDateTime.parse)
      .toRight(DecodingFailure(s"Attempt to decode non-timestamp as ZonedDateTime: ${c.value}", List()))
  }

  implicit def listDecoder[A: Decoder]: Decoder[List[A]] =
    Decoder.decodeOption(Decoder.decodeList[A]).map(_.getOrElse(List.empty))

  implicit val memberUserDecoder: Decoder[MemberUser] = (c: HCursor) => {
    val user = c.as[User]
    user.flatMap { user =>
      val memberJ = c
        .downField("member")
        .success
        .toRight(
          DecodingFailure(
            s"Attempt to decode member from non-MemberUser: ${c.value}",
            List(CursorOp.DownField("member"))
          )
        )
      memberJ.flatMap(_.as[PartialMember].map(member => MemberUser(user, member)))
    }
  }

}
