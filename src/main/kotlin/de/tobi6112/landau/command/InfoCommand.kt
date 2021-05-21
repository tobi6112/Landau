package de.tobi6112.landau.command

import discord4j.core.`object`.entity.ApplicationInfo
import discord4j.core.event.domain.message.MessageCreateEvent
import java.time.Instant

/**
 * Command for information about the bot
 *
 * @property applicationInfo application info
 */
class InfoCommand(private val applicationInfo: ApplicationInfo) : Command("info") {
  override fun run(event: MessageCreateEvent) {
    val message = event.message
    message.channel.subscribe { channel ->
      channel.createEmbed { spec ->
        spec.setTitle("${applicationInfo.name} information")
        spec.addField("Author", "<@${applicationInfo.ownerId.asLong()}>", true)
        spec.setTimestamp(Instant.now())
      }.block()
    }
  }
}
