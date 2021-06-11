package de.tobi6112.landau.discord

import discord4j.core.GatewayDiscordClient
import mu.KotlinLogging

/**
 * Holds applicationInfo
 *
 * @param client client
 */
@Suppress("USE_DATA_CLASS", "MISSING_KDOC_CLASS_ELEMENTS")
class ApplicationInfo(client: GatewayDiscordClient) {
  private val log = KotlinLogging.logger {}
  private val info =
      client
          .applicationInfo
          .doOnSuccess { info ->
            log.info {
              val owner = info.owner.block()!!
              "Started ${info.name} by ${owner.username + "#" + owner.discriminator}"
            }
          }
          .doOnError { err -> log.error(err) { "Could not get ApplicationInfo" } }
          .block()
  val applicationName: String = this.info.name
  val applicationId: Long = this.info.id.asLong()
}
