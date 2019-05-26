package org.discordscala.discocat

package model

import cats.ApplicativeError
import fs2.{io => _, _}
import io.circe.Json
import io.circe.fs2._
import io.circe.generic.auto._
import io.circe.generic.extras.{ConfiguredJsonCodec, JsonKey}
import java.time.ZonedDateTime
import spire.math.ULong

sealed trait Message {

  val id: ULong
  val channelId: ULong
  val author: User
  val content: String
  val timestamp: ZonedDateTime
  val editedTimestamp: Option[ZonedDateTime]
  val tts: Boolean
  val mentionEveryone: Boolean
  val mentions: List[User]
  val mentionRoles: List[ULong]
  val attachments: List[Json] // TODO
  val embeds: List[Json] // TODO
  val pinned: Boolean

}

sealed trait GuildMessage extends Message {

  val guildId: ULong
  val member: PartialMember
  override val mentions: List[MemberUser]

}

@ConfiguredJsonCodec case class WebhookMessage(
  id: ULong,
  channelId: ULong,
  guildId: ULong,
  author: User,
  member: PartialMember,
  content: String,
  timestamp: ZonedDateTime,
  editedTimestamp: Option[ZonedDateTime],
  tts: Boolean,
  mentionEveryone: Boolean,
  mentions: List[MemberUser]
) extends GuildMessage

@ConfiguredJsonCodec case class MessageO(
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
  @JsonKey("type") msgType: MessageType,
  activity: Option[Json], // TODO
  application: Option[Json] // TODO
) {

  val user: Option[MemberUser] = member.map(MemberUser(author, _))

  def channel[F[_]](implicit c: Client[F], raiseThrowable: ApplicativeError[F, Throwable]): Stream[F, Channel] =
    c.request.get(s"channels/$channelId", Nil).flatMap(_.body).through(byteStreamParser[F]).through(decoder[F, Channel])

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

sealed trait MessageType {

  val code: Int

}

object MessageType {

  val map: Map[Int, MessageType] =
    (Default
      :: RecipientAdd
      :: RecipientRemove
      :: Call
      :: ChannelNameChange
      :: ChannelIconChange
      :: ChannelPinnedMessage
      :: GuildMemberJoin
      :: Nil).map(t => (t.code, t)).toMap

  def apply(i: Int): MessageType = map(i)

  case object Default extends MessageType {
    override val code: Int = 0
  }

  case object RecipientAdd extends MessageType {
    override val code: Int = 1
  }

  case object RecipientRemove extends MessageType {
    override val code: Int = 2
  }

  case object Call extends MessageType {
    override val code: Int = 3
  }

  case object ChannelNameChange extends MessageType {
    override val code: Int = 4
  }

  case object ChannelIconChange extends MessageType {
    override val code: Int = 5
  }

  case object ChannelPinnedMessage extends MessageType {
    override val code: Int = 6
  }

  case object GuildMemberJoin extends MessageType {
    override val code: Int = 7
  }

}
