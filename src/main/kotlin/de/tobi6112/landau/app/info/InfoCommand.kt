package de.tobi6112.landau.app.info

import de.tobi6112.landau.app.core.command.AbstractCommand
import de.tobi6112.landau.app.core.command.Choice
import de.tobi6112.landau.app.core.command.Option
import de.tobi6112.landau.app.core.command.OptionType
import de.tobi6112.landau.app.misc.DiscordLiterals
import de.tobi6112.landau.app.misc.Emoji
import de.tobi6112.landau.util.DefaultPerformanceMonitor
import de.tobi6112.landau.util.PerformanceMonitor
import discord4j.core.`object`.entity.Guild
import discord4j.core.event.domain.InteractionCreateEvent
import discord4j.rest.util.Color
import mu.KotlinLogging
import reactor.core.publisher.Mono

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Properties

/** Information command */
class InfoCommand(
    private val appName: String,
    private val description: String,
    private val iconUrl: String = "https://cdn.discordapp.com/embed/avatars/0.png",
    private val performanceMonitor: PerformanceMonitor = DefaultPerformanceMonitor()
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
              .setTitle("Bot Information")
              .setColor(Color.SEA_GREEN)
              .setDescription(description)
              .setAuthor(appName, null, iconUrl)
              .addField("${Emoji.PUSHPIN} Version", version, true)
              .addField("${Emoji.BOOKMARK} Library", "[Discord4J](https://discord4j.com/)", true)
              .addField("${Emoji.TRIANGULAR_FLAG} Language", "Kotlin", true)
              .addField("${Emoji.GEAR} Performance", buildPerformanceString(), true)
        }
      }

  private fun getServerInfo(event: InteractionCreateEvent) =
      event.interaction.guild.flatMap { guild ->
        event.reply { interaction ->
          interaction.addEmbed { embed ->
            val formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
            embed
                .setAuthor(appName, null, iconUrl)
                .setTitle("Server Information: ${guild.name}")
                .setColor(Color.SEA_GREEN)
                .addField(
                    "${Emoji.CROWN} Owner",
                    DiscordLiterals.MENTION_LITERAL.buildLiteral(guild.ownerId.asString()),
                    true)
                .addField("${Emoji.PEOPLE_HUGGING} Members", guild.memberCount.toString(), true)
                .addField("${Emoji.ROCKET} Server Boost", buildServerBoostString(guild), true)
                .addField("${Emoji.BIRTHDAY} Birthday", buildBirthdayString(guild.joinTime), true)
          }
        }
      }

  private fun loadVersion(): String {
    val prop = Properties()
    prop.load(this::class.java.getResource("/version.properties")!!.openStream())
    return prop.getProperty("version")!!
  }

  @Suppress("MAGIC_NUMBER")
  private fun buildPerformanceString(): String {
    val memoryUsageInMb = performanceMonitor.usedMemory() / (1_000_000.toDouble())
    val availableMemoryInMb = performanceMonitor.availableMemory() / (1_000_000.toDouble())
    return """
      Memory usage: ${memoryUsageInMb.format(2)} / ${availableMemoryInMb.format(2)} MB
      CPU load: ${performanceMonitor.cpuUsage().format(2)}% (${performanceMonitor.availableProcessors()} cores available)
    """.trimIndent()
  }

  private fun buildServerBoostString(guild: Guild): String {
    // Server Boost level
    val level = guild.premiumTier.value

    // Number of boosts
    val num: Int = guild.premiumSubscriptionCount.orElse(0)

    return """
      Level: $level
      Boosts: $num
    """.trimIndent()
  }

  private fun buildBirthdayString(joinTime: Instant): String {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault())

    val date = LocalDate.ofInstant(joinTime, ZoneId.systemDefault())

    return "Birthday: ${formatter.format(date)}".trimIndent()
  }
}

/** @param digits */
fun Double.format(digits: Int) = "%.${digits}f".format(this)
