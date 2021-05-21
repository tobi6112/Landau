package de.tobi6112.landau

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import discord4j.core.DiscordClient

/**
 * Main class of Landau Bot, processes CLI parameters and creates DiscordClient
 */
class Landau : CliktCommand() {
    private val token by option("-t", "--token", help = "Bot token", envvar = "LANDAU_BOT_TOKEN").required()

    override fun run() {
        val client = DiscordClient.create(token)
    }
}

fun main(args: Array<String>) = Landau().main(args)
