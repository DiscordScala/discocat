package org.discordscala.discocat

package model

import io.circe._
import io.circe.syntax._

trait Implicits {

  implicit val messageTypeEncoder: Encoder[MessageType] = _.code.asJson
  implicit val messageTypeDecoder: Decoder[MessageType] = _.as[Int].map(MessageType(_))

}
