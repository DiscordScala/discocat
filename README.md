[![](https://img.shields.io/codacy/grade/8a52090e000f44d0b99f8fcdf80b6cff.svg?style=flat-square)](https://www.codacy.com/app/srn/discocat?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=sorenbug/discocat&amp;utm_campaign=Badge_Grade)
[![](https://img.shields.io/sonar/https/sonarcloud.io/sorenbug_discocat/violations.svg?format=long&style=flat-square)](https://sonarcloud.io/dashboard?id=sorenbug_discocat)
[![](https://img.shields.io/librariesio/github/sorenbug/discocat.svg?style=flat-square)](https://libraries.io/github/sorenbug/discocat)
[![](https://img.shields.io/github/license/sorenbug/discocat.svg?style=flat-square)](https://www.gnu.org/licenses/lgpl-3.0.en.html)
[![](https://img.shields.io/discord/390751088829005826.svg?style=flat-square)](https://discord.gg/TQZ5Brw)

# discocat

Discocat is a Scala library that provides access to Discord's API built around [cats](https://typelevel.org/cats/) and [fs2](https://fs2.io/). It is under the LGPL v3.

## What this license means for you

It means:
  - When making changes to this library, they have to be made completely public.
  - When making a bot, either:
    - The bot's source must be made completely public.
    - The bot must be able to display its usage of this library.
  
## Examples

All examples are written in terms of `IOApp` and `program[F[_]]`. These implicits are needed:
  - `AsynchronousChannelGroup`
  - `ConcurrentEffect[F]`
  - `ContextShift[F]`
  - `Timer[F]`

### Ping

A simple `!ping` -> `Pong` bot:

```scala
for {
  t <- Sync[F].delay(StdIn.readLine("Token? "))
  c <- Client[F](t)
  l <- c
    .login(
      EventHandler[F] {
        case MessageCreate(_, m) =>
          if (m.content == "!ping") {
            c.request.post(s"channels/${m.channelId}/messages", Nil, Map("content" -> "Pong!")).drain
          } else {
            Stream.empty
          }
      }
        :: Defaults.defaultEventHandler[F]
        :: Nil
    )
    .compile
    .drain
} yield l
```
