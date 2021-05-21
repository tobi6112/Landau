package de.tobi6112.landau

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import discord4j.core.DiscordClient
import mu.KotlinLogging
import java.time.Duration

/**
 * Main class of Landau Bot, processes CLI parameters and creates DiscordClient
 */
class Landau : CliktCommand() {
  private val logger = KotlinLogging.logger {}

  // CLI Options
  private val token by option("-t", "--token", help = "Bot token", envvar = "LANDAU_BOT_TOKEN").required()

  @Suppress("MAGIC_NUMBER")
  override fun run() {
    val client = DiscordClient.create(token)
    val gateway = client.login()
        .doOnSuccess { logger.info { "Client successfully logged in" } }
        .doOnError { logger.error { "Client couldn't login" } }
        .block(Duration.ofSeconds(15))

    gateway.onDisconnect()
        .doFinally { logger.info { "Client disconnected" } }
        .block()
  }
}

fun main(args: Array<String>) = Landau().main(args)
