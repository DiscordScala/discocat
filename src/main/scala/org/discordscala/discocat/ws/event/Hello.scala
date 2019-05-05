package org.discordscala.discocat

package ws.event

import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.generic.extras.ConfiguredJsonCodec
import org.discordscala.discocat.Client
import org.discordscala.discocat.ws.Event
import spire.math.ULong

case class Hello[F[_]](client: Client[F], d: HelloData) extends Event[F] {

  override type A = HelloData

  override val op: Int = 10
  override val t: Option[String] = None
  override val encoder: Encoder[HelloData] = implicitly[Encoder[HelloData]]

}

@ConfiguredJsonCodec case class HelloData(heartbeatInterval: ULong)
