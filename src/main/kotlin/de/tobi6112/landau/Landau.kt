package de.tobi6112.landau

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import discord4j.core.DiscordClient
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
  private val systemProperties: Map<String, String> by option("-D").associate()

  @Suppress("MAGIC_NUMBER")
  override fun run() {
    // Set system properties
    systemProperties.entries.forEach {
      System.setProperty(it.key, it.value)
    }

    val client = DiscordClient
        .builder(token)
        .setDefaultAllowedMentions(AllowedMentions.suppressEveryone())
        .build()
        .login()
        .block()

    val applicationInfo = client.applicationInfo.block()
    logger.info {
      val owner = applicationInfo.owner.block()
      "Starting ${applicationInfo.name} by ${owner.username + "#" + owner.discriminator}"
    }

    client.onDisconnect().block()
    logger.info { "Shutting down..." }
    exitProcess(0)
  }
}

fun main(args: Array<String>) = Landau().main(args)
