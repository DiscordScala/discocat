package org.discordscala.discocat

package model

import io.circe.Json
import io.circe.generic.auto._
import io.circe.generic.extras.{ConfiguredJsonCodec, JsonKey}
import java.time.ZonedDateTime
import spire.math.ULong

@ConfiguredJsonCodec case class Channel(
  id: ULong,
  @JsonKey("type") chanType: Int, // TODO
  guildId: Option[ULong],
  position: Option[Int],
  permissionOverwrites: List[Json], // TODO
  name: Option[String],
  topic: Option[String],
  nsfw: Option[Boolean],
  lastMessageId: Option[ULong],
  bitrate: Option[Int],
  userLimit: Option[Int],
  rateLimitPerUser: Option[Int],
  recipients: List[User],
  @JsonKey("icon") iconHash: Option[String],
  ownerId: Option[ULong],
  parentId: Option[ULong],
  lastPinTimestamp: Option[ZonedDateTime]
)
