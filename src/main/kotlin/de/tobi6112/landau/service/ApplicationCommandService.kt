package de.tobi6112.landau.service

import de.tobi6112.landau.command.core.AbstractCommand
import de.tobi6112.landau.config.CommandsConfig
import discord4j.core.`object`.entity.ApplicationInfo
import discord4j.rest.service.ApplicationService
import mu.KotlinLogging

/**
 * Application command service
 *
 * @property applicationInfo application info
 * @property applicationService application service
 */
class ApplicationCommandService(
    private val applicationInfo: ApplicationInfo,
    private val applicationService: ApplicationService
) {
  private val logger = KotlinLogging.logger {}
  private val applicationId = applicationInfo.id.asLong()

  /**
   * Create application Commands based on the provided configuration.
   *
   * @param commands available commands
   * @param config commands config
   */
  fun createCommands(commands: Iterable<AbstractCommand>, config: CommandsConfig) {
    // Get global enabled commands
    val globalEnabledCommands = commands.filter { config.global[it.name]?.enabled ?: true }
    createGlobalCommands(globalEnabledCommands)

    // Get guild enabled commands
    val guildEnabledCommands =
        config.guilds.mapValues { (_, cmdCfg) ->
          commands.filter { cmdCfg[it.name]?.enabled ?: true }
        }
    guildEnabledCommands.forEach { createGuildCommands(it.key, it.value) }
  }

  private fun createGlobalCommands(commands: Iterable<AbstractCommand>) {
    checkMultipleCommands(commands)

    // TODO Return Mono
    commands.distinctBy { it.name }.forEach {
      val request = it.toRequest()
      this.applicationService
          .createGlobalApplicationCommand(applicationId, request)
          .doOnError { err -> logger.error(err) { "Could not create global command $it" } }
          .doOnSuccess { logger.info { "Successfully created global command $it" } }
          .subscribe()
    }
  }

  private fun createGuildCommands(guildId: Long, commands: Iterable<AbstractCommand>) {
    checkMultipleCommands(commands)

    // TODO Return Mono
    commands.distinctBy { it.name }.forEach { command ->
      val request = command.toRequest()
      this.applicationService
          .createGuildApplicationCommand(applicationId, guildId, request)
          .doOnError { err ->
            logger.error(err) { "Could not create guild command $command on guild $guildId" }
          }
          .doOnSuccess {
            logger.info { "Successfully created global command $command on guild $guildId" }
          }
          .subscribe()
    }
  }

  private fun checkMultipleCommands(commands: Iterable<AbstractCommand>) {
    commands.groupingBy { it.name }.eachCount().filter { it.value > 1 }
        .forEach { (name, count) ->
          logger.warn { "Multiple commands for name $name found ($count times)" }
        }
  }
}
