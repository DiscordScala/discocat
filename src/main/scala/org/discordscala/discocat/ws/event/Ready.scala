package org.discordscala.discocat

package ws.event

import io.circe.{Encoder, Json}
import io.circe.generic.auto._
import io.circe.generic.extras.ConfiguredJsonCodec
import org.discordscala.discocat.model.User
import org.discordscala.discocat.ws.Event

case class Ready[F[_]](client: Client[F], d: ReadyData) extends Event[F] {

  override type A = ReadyData
  override val encoder: Encoder[ReadyData] = implicitly[Encoder[ReadyData]]

  override val op: Int = 0
  override val t: Option[String] = Some("READY")
}

@ConfiguredJsonCodec case class ReadyData(
  v: Int,
  user: User,
  guilds: List[Json], // TODO
  sessionId: String
)
