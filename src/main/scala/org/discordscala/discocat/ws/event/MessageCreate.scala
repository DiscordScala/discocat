package org.discordscala.discocat

package ws.event

import io.circe.Encoder
import org.discordscala.discocat.model.Message
import org.discordscala.discocat.ws.Event

case class MessageCreate[F[_]](client: Client[F], d: Message) extends Event[F] {

  override type A = Message
  override val encoder: Encoder[Message] = implicitly[Encoder[Message]]

  override val op: Int = 0
  override val t: Option[String] = Some("MESSAGE_CREATE")
}
