package de.tobi6112.landau

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import de.tobi6112.landau.command.Command
import de.tobi6112.landau.command.InfoCommand
import de.tobi6112.landau.config.Configuration
import de.tobi6112.landau.service.ApplicationCommandService
import discord4j.core.DiscordClient
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.rest.util.AllowedMentions
import mu.KotlinLogging
import kotlin.system.exitProcess

/**
 * Main class of Landau Bot, processes CLI parameters and creates DiscordClient
 */
class Landau : CliktCommand() {
  private val logger = KotlinLogging.logger {}

  // CLI Options
  private val token by option("-t", "--token", help = "Bot token", envvar = "LANDAU_BOT_TOKEN").required()
  private val configEnv by option("-c", "--config", help = "Configuration environment")
  private val systemProperties: Map<String, String> by option("-D").associate()

  // TODO temporary solution
  private val applicationCommands: Iterable<Command> = listOf(InfoCommand())

  @Suppress("MAGIC_NUMBER")
  override fun run() {
    // Set system properties
    systemProperties.entries.forEach {
      System.setProperty(it.key, it.value)
    }

    val config = Configuration.getConfig(configEnv)

    val client = DiscordClient
        .builder(token)
        .setDefaultAllowedMentions(AllowedMentions.suppressEveryone())
        .build()
        .login()
        .block()

    val applicationInfo = client.applicationInfo.block()
    logger.info {
      val owner = applicationInfo.owner.block()
      "Started ${applicationInfo.name} by ${owner.username + "#" + owner.discriminator}"
    }

    client.on(ReadyEvent::class.java)
        .doOnNext {
          logger.info { "${applicationInfo.name} is ready..." }
        }
        .blockFirst()

    // Register all applicationCommands
    val applicationCommandService = ApplicationCommandService(applicationInfo, client.restClient.applicationService)
    applicationCommandService.createCommands(applicationCommands, config.bot.commands)

    client.onDisconnect().block()
    logger.info { "Shutting down..." }
    exitProcess(0)
  }
}

fun main(args: Array<String>) = Landau().main(args)
