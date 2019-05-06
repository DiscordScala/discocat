package org.discordscala.discocat

package ws.event

import io.circe.Encoder
import org.discordscala.discocat.ws.Event

case class HeartbeatAck[F[_]](client: Client[F]) extends Event[F] {
  override type A = None.type
  override val encoder: Encoder[None.type] = implicitly[Encoder[None.type]]
  override val d: None.type = None
  override val op: Int = 11
  override val t: Option[String] = None
}
