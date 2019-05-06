package org.discordscala.discocat

package ws.event

import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.generic.extras.{ConfiguredJsonCodec, JsonKey}
import org.discordscala.discocat.ws.Event

case class Identify[F[_]](client: Client[F], d: IdentifyData) extends Event[F] {

  override type A = IdentifyData
  override val encoder: Encoder[IdentifyData] = implicitly[Encoder[IdentifyData]]

  override val op: Int = 2
  override val t: Option[String] = None

}

@ConfiguredJsonCodec case class IdentifyData(token: String, properties: IdentifyProperties = IdentifyProperties())

case class IdentifyProperties(
  @JsonKey("$os") os: String = "linux",
  @JsonKey("$browser") browser: String = "discocat",
  @JsonKey("$device") device: String = "discocat"
)
