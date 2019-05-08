package org.discordscala.discocat

package model

import io.circe.generic.auto._
import io.circe.generic.extras.ConfiguredJsonCodec
import java.time.ZonedDateTime
import spire.math.ULong

@ConfiguredJsonCodec case class Member(
  user: User,
  nick: Option[String],
  roles: List[ULong],
  joinedAt: ZonedDateTime,
  deaf: Boolean,
  mute: Boolean
)

@ConfiguredJsonCodec case class PartialMember(
  nick: Option[String],
  roles: List[ULong],
  joinedAt: ZonedDateTime,
  deaf: Boolean,
  mute: Boolean
) {

  def apply(u: User) = Member(u, nick, roles, joinedAt, deaf, mute)

}
