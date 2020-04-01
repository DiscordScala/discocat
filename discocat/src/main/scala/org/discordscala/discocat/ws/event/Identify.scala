package org.discordscala.discocat

package ws.event

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import io.circe.generic.extras.JsonKey
import org.discordscala.discocat.ws.Event

case class Identify[F[_]](client: Client[F], d: IdentifyData) extends Event[F] {

  override type A = IdentifyData
  override val encoder: Encoder[IdentifyData] = IdentifyData.identifyDataEncoder

  override val op: Int = 2
  override val t: Option[String] = None

}

case class IdentifyData(token: String, properties: IdentifyProperties = IdentifyProperties())

object IdentifyData {

  implicit val identifyDataEncoder: Encoder[IdentifyData] = deriveEncoder
  implicit val identifyDataDecoder: Decoder[IdentifyData] = deriveDecoder

}

case class IdentifyProperties(
  @JsonKey(s"$$os") os: String = "linux",
  @JsonKey(s"$$browser") browser: String = "discocat",
  @JsonKey(s"$$device") device: String = "discocat"
)

object IdentifyProperties {

  implicit val identifyPropertiesEncoder: Encoder[IdentifyProperties] = deriveEncoder
  implicit val identifyPropertiesDecoder: Decoder[IdentifyProperties] = deriveDecoder

}
