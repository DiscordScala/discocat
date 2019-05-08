package org.discordscala.discocat

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import cats._
import cats.implicits._
import io.circe.Encoder
import org.discordscala.discocat.model.{MemberUser, Message, User}
import scodec.{Attempt, Err}
import scodec.bits.ByteVector
import spinoco.fs2.http.body.BodyEncoder
import spinoco.protocol.mime.{ContentType, MIMECharset, MediaType}

object instances {

  implicit val userShow: Show[User] = (t: User) => s"${t.username}#${t.discriminator} (${t.id})"

  implicit val timestampShow: Show[ZonedDateTime] = (t: ZonedDateTime) => t.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)

  implicit val memberUserShow: Show[MemberUser] = (t: MemberUser) =>
    t.member.nick.fold("")(a => "\"" ++ a ++ "\" ") ++ show"${t.user}"

  implicit def jsonBodyEncoder[A](implicit enc: Encoder[A]): BodyEncoder[A] =
    BodyEncoder.instance[A](ContentType.TextContent(MediaType.`application/json`, Some(MIMECharset.`UTF-8`))) { a =>
      ByteVector.encodeUtf8(enc(a).noSpaces) match {
        case Right(s) => Attempt.Successful(s)
        case Left(e) => Attempt.Failure(Err.apply(e.getLocalizedMessage))
      }
    }

}
