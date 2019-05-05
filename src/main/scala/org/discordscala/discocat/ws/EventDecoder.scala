package org.discordscala.discocat.ws

import io.circe.DecodingFailure
import org.discordscala.discocat.Client

trait EventDecoder {

  def decode[F[_]](client: Client[F], struct: EventStruct): Either[DecodingFailure, Event[F]]

}
