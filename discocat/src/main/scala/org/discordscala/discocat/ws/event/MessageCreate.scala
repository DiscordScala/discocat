package org.discordscala.discocat

package ws.event

import io.circe.Encoder
import org.discordscala.discocat.model.MessageLike
import org.discordscala.discocat.ws.Event

case class MessageCreate[F[_]](client: Client[F], d: MessageLike) extends Event[F] {

  override type A = MessageLike
  override val encoder: Encoder[MessageLike] = Encoder.instance(_ => ???)

  override val op: Int = 0
  override val t: Option[String] = Some("MESSAGE_CREATE")
}
