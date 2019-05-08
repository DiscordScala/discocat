package org.discordscala.discocat

package model

import io.circe.generic.auto._
import io.circe.generic.extras.ConfiguredJsonCodec
import spire.math.ULong

@ConfiguredJsonCodec case class User(
  id: ULong,
  username: String,
  discriminator: String,
  avatar: Option[String],
  bot: Option[Boolean],
  mfaEnabled: Option[Boolean],
  locale: Option[String],
  verified: Option[Boolean],
  email: Option[String],
  flags: Option[Int], // TODO
  premiumType: Option[Int] // TODO
)

case class MemberUser(
  user: User,
  member: PartialMember
)
