package org.discordscala

import cats.effect.concurrent.Ref
import fs2.{io => _, _}
import io.circe.generic.extras._
import io.circe.{Decoder, DecodingFailure, HCursor}
import org.discordscala.discocat.ws.Event
import scala.util.Try
import spire.math.ULong

package object discocat {

  type EventHandler[F[_]] = Ref[F, Option[ULong]] => Pipe[F, Event[F], Unit]
  type EventHandlers[F[_]] = List[EventHandler[F]]

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val ulongDecoder: Decoder[ULong] = (c: HCursor) => {
    val numO = c.value.asNumber
    val strO = c.value.asString
    val numAsBI = numO.flatMap(_.toBigInt)
    val strAsBI = strO.flatMap(s => Try(BigInt(s)).toOption)
    val biO = numAsBI.map(Some(_)).getOrElse(strAsBI)
    biO.map(ULong.fromBigInt).toRight(DecodingFailure(s"Attempt to decode non-integer as ULong: ${c.value}", List()))
  }

}
