package org.discordscala.discocat

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import cats._
import cats.implicits._
import org.discordscala.discocat.model.{MemberUser, Message, User}

object instances {

  implicit val userShow: Show[User] = (t: User) => s"${t.username}#${t.discriminator} (${t.id})"

  implicit val timestampShow: Show[ZonedDateTime] = (t: ZonedDateTime) => t.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)

  implicit val memberUserShow: Show[MemberUser] = (t: MemberUser) =>
    t.member.nick.fold("")(a => "\"" ++ a ++ "\" ") ++ show"${t.user}"

}
