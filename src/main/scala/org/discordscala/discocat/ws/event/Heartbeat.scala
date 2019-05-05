package org.discordscala.discocat

package ws.event

import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.discordscala.discocat.ws.Event
import spire.math.ULong

case class Heartbeat[F[_]](client: Client[F], d: Option[ULong]) extends Event[F] {

  override type A = Option[ULong]
  override val encoder: Encoder[Option[ULong]] = (u: Option[ULong]) => u.fold(Json.Null)(_.toBigInt.asJson)

  override val op: Int = 1
  override val t: Option[String] = None

}
