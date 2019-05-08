package org.discordscala.discocat

package model

import io.circe.Json
import io.circe.generic.auto._
import io.circe.generic.extras.{ConfiguredJsonCodec, JsonKey}
import java.time.ZonedDateTime

import spire.math.ULong

@ConfiguredJsonCodec case class Message(
  id: ULong,
  channelId: ULong,
  guildId: Option[ULong],
  author: User,
  member: Option[PartialMember],
  content: String,
  timestamp: ZonedDateTime,
  editedTimestamp: Option[ZonedDateTime],
  tts: Boolean,
  mentionEveryone: Boolean,
  mentions: List[MemberUser],
  mentionRoles: List[ULong],
  attachments: List[Json], // TODO
  embeds: List[Json], // TODO
  reactions: List[Json], // TODO
  nonce: Option[ULong],
  pinned: Boolean,
  webhookId: Option[ULong],
  @JsonKey("type") msgType: Int, // TODO
  activity: Option[Json], // TODO
  application: Option[Json] // TODO
) {

  val user: Option[MemberUser] = member.map(MemberUser(author, _))

}

object Message {

  object Ct {
    def unapply(arg: Message): Option[String] = Some(arg.content)
  }

  object ChCt {
    def unapply(arg: Message): Option[(ULong, String)] = Some((arg.channelId, arg.content))
  }

  object AuChCt {
    def unapply(arg: Message): Option[(User, ULong, String)] = Some((arg.author, arg.channelId, arg.content))
  }

}
