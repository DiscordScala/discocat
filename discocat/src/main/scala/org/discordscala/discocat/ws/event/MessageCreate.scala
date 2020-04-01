package org.discordscala.discocat

package ws.event

import io.circe.{Encoder, Json}
import org.discordscala.discocat.ws.Event

case class MessageCreate[F[_]](client: Client[F], d: Json) extends Event[F] {

  override type A = Json
  override val encoder: Encoder[Json] = implicitly[Encoder[Json]]

  override val op: Int = 0
  override val t: Option[String] = Some("MESSAGE_CREATE")
}
