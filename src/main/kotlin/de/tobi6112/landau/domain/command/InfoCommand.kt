package de.tobi6112.landau.domain.command

import de.tobi6112.landau.domain.command.core.AbstractCommand
import de.tobi6112.landau.domain.command.core.Choice
import de.tobi6112.landau.domain.command.core.Option
import de.tobi6112.landau.domain.command.core.OptionType
import de.tobi6112.landau.domain.misc.Emoji
import discord4j.core.event.domain.InteractionCreateEvent
import discord4j.rest.util.Color
import mu.KotlinLogging
import reactor.core.publisher.Mono

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Properties

/** Information command */
class InfoCommand(
    private val appName: String,
    private val description: String,
    private val iconUrl: String = "https://cdn.discordapp.com/embed/avatars/0.png"
) :
  AbstractCommand(
    name = "info",
    description = "Retrieve information",
    options =
        listOf(
            Option(
              type = OptionType.STRING,
              name = "subject",
              description = "The subject to retrieve information about",
              required = true,
              choices = listOf(Choice("bot", "bot"), Choice("server", "server"))))) {
  private val logger = KotlinLogging.logger {}
  private val version: String = this.loadVersion()

  /**
   * Handles a info command
   *
   * @param event Interaction event
   * @return ?
   */
  override fun handleEvent(event: InteractionCreateEvent): Mono<Void> {
    val interaction = event.interaction.commandInteraction
    val optSubject = interaction.getOption("subject")
    if (optSubject.isEmpty) {
      logger.error { "Somehow subject is empty, this should not happen as it is required" }
      return Mono.error(RuntimeException("subject is empty"))
    }
    val subject = optSubject.get()

    val optValue = subject.value
    if (optValue.isEmpty) {
      logger.error { "Somehow subject value is empty, this should not happen" }
      return Mono.error(RuntimeException("subject value is empty"))
    }
    val value = subject.value

    if (subject.type.value != OptionType.STRING.value) {
      logger.error { "Option Type is not a string, this should not happen" }
      return Mono.error(RuntimeException("Option type is not a string"))
    }

    return when (value.get().asString()) {
      "bot" -> getBotInfo(event)
      "server" -> getServerInfo(event)
      else -> Mono.error(RuntimeException("Unknown value ${value.get().asString()}"))
    }
  }

  private fun getBotInfo(event: InteractionCreateEvent) =
      event.reply { ev ->
        ev.addEmbed { embed ->
          embed
              .setTitle("Information")
              .setColor(Color.SEA_GREEN)
              .setDescription(description)
              .setAuthor(appName, null, iconUrl)
              .addField("${Emoji.PUSHPIN} Version", version, true)
              .addField("${Emoji.BOOKMARK} Library", "[Discord4J](https://discord4j.com/)", true)
              .addField("${Emoji.TRIANGULAR_FLAG} Language", "Kotlin", true)
        }
      }

  private fun getServerInfo(event: InteractionCreateEvent): Mono<Void> {
    val guild = event.interaction.guild.block()!!
    return event.reply { interaction ->
      interaction.addEmbed { embed ->
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
        embed
            .setAuthor(appName, null, iconUrl)
            .setTitle("Server Information: ${guild.name}")
            .addField("${Emoji.CROWN} Owner", "<@${guild.ownerId.asLong()}>", true)
            .addField("${Emoji.PEOPLE_HUGGING} Members", guild.memberCount.toString(), true)
            .addField("${Emoji.BIRTHDAY} Birthday", formatter.format(guild.joinTime), true)
      }
    }
  }

  private fun loadVersion(): String {
    val prop = Properties()
    prop.load(this::class.java.getResource("/version.properties")!!.openStream())
    return prop.getProperty("version")!!
  }
}
