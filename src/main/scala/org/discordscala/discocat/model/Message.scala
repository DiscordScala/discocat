package org.discordscala.discocat
package model

import io.circe.Json
import io.circe.generic.auto._
import io.circe.generic.extras.ConfiguredJsonCodec
import spire.math.ULong

@ConfiguredJsonCodec case class Message(
  id: ULong,
  channelId: ULong,
  guildId: Option[ULong],
  author: Json, // TODO
  member: Option[Json], // TODO
  content: String,
  // TODO
)
