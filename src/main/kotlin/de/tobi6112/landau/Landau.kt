package de.tobi6112.landau

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import de.tobi6112.landau.command.Command
import de.tobi6112.landau.command.core.AbstractCommand
import de.tobi6112.landau.command.service.ApplicationCommandService
import de.tobi6112.landau.core.config.Configuration
import de.tobi6112.landau.data.Database
import discord4j.core.DiscordClient
import discord4j.core.event.domain.InteractionCreateEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.rest.util.AllowedMentions
import mu.KotlinLogging
import reactor.core.publisher.Mono

import kotlin.system.exitProcess

/** Main class of Landau Bot, processes CLI parameters and creates DiscordClient */
class Landau : CliktCommand() {
  private val logger = KotlinLogging.logger {}

  // CLI Options
  private val token by option("-t", "--token", help = "Bot token", envvar = "LANDAU_BOT_TOKEN")
      .required()
  private val configEnv by option("-c", "--config", help = "Configuration environment")
  private val systemProperties: Map<String, String> by option("-D").associate()

  @Suppress("MAGIC_NUMBER")
  override fun run() {
    // Set system properties
    systemProperties.entries.forEach { System.setProperty(it.key, it.value) }

    val config = Configuration.getConfig(configEnv)

    Database.connect(
        url = config.database.jdbcUrl,
        driver = config.database.driver,
        user = config.database.username,
        password = config.database.password)

    val client =
        DiscordClient.builder(token)
            .setDefaultAllowedMentions(AllowedMentions.suppressEveryone())
            .build()
            .login()
            .block()

    val applicationInfo =
        client!!
            .applicationInfo
            .doOnSuccess { info ->
              logger.info {
                val owner = info.owner.block()!!
                "Started ${info.name} by ${owner.username + "#" + owner.discriminator}"
              }
            }
            .doOnError { err -> logger.error(err) { "Could not get ApplicationInfo" } }
            .block()

    // TODO temporary solution
    val applicationCommands = Command.getAllCommands()

    client
        .on(ReadyEvent::class.java)
        .doOnNext { logger.info { "${applicationInfo!!.name} is ready..." } }
        .blockFirst()

    // Register all applicationCommands
    val applicationCommandService =
        ApplicationCommandService(
            applicationInfo!!, client.restClient.applicationService, config.bot.commands)

    val commands: MutableMap<Long, AbstractCommand> = mutableMapOf()

    applicationCommandService
        .createCommands(applicationCommands)
        .doOnNext {
          if (commands.containsKey(it.first)) {
            logger.warn { "Command with id ${it.first} already exist" }
          } else {
            commands[it.first] = it.second
          }
        }
        .subscribe()

    client
        .eventDispatcher
        .on(InteractionCreateEvent::class.java)
        .flatMap { event ->
          val command = commands[event.commandId.asLong()]
          command
              ?: run {
                logger.debug {
                  "No command with ID ${event.commandId.asLong()} (name: ${event.commandName}) found"
                }
                return@flatMap Mono.empty()
              }
          return@flatMap command.handleEvent(event)
        }
        .subscribe()

    client.onDisconnect().block()
    logger.info { "Shutting down..." }
    exitProcess(0)
  }

  private fun connectWithDatabase() {
  }
}

fun main(args: Array<String>) = Landau().main(args)
