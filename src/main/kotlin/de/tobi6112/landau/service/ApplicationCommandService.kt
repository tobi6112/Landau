package de.tobi6112.landau.service

import de.tobi6112.landau.config.CommandsConfig
import de.tobi6112.landau.domain.command.core.AbstractCommand
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
   * @return map id <-> Command
   */
  fun createCommands(
      commands: Iterable<AbstractCommand>,
      config: CommandsConfig
  ): Map<Long, AbstractCommand> {
    // Get global enabled commands
    val globalEnabledCommands = commands.filter { config.global[it.name]?.enabled ?: true }
    val globalCommands = createGlobalCommands(globalEnabledCommands)

    // Get guild enabled commands
    val guildEnabledCommands =
        config.guilds.mapValues { (_, cmdCfg) ->
          commands.filter { cmdCfg[it.name]?.enabled ?: true }
        }
    val guildCommands: MutableMap<Long, AbstractCommand> = mutableMapOf()
    guildEnabledCommands.forEach { guildCommands.putAll(createGuildCommands(it.key, it.value)) }

    guildCommands.putAll(globalCommands)
    return guildCommands.toMap()
  }

  @Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")
  private fun createGlobalCommands(
      commands: Iterable<AbstractCommand>
  ): Map<Long, AbstractCommand> {
    checkMultipleCommands(commands)

    val map: MutableMap<Long, AbstractCommand> = mutableMapOf()

    // TODO Return Mono
    commands.distinctBy { it.name }.forEach {
      val request = it.toRequest()
      val res =
          this.applicationService
              .createGlobalApplicationCommand(applicationId, request)
              .doOnError { err -> logger.error(err) { "Could not create global command $it" } }
              .doOnSuccess { logger.info { "Successfully created global command $it" } }
              .block()

      map[res.id().toLong()] = it
    }

    return map
  }

  private fun createGuildCommands(
      guildId: Long,
      commands: Iterable<AbstractCommand>
  ): Map<Long, AbstractCommand> {
    checkMultipleCommands(commands)

    val map: MutableMap<Long, AbstractCommand> = mutableMapOf()

    // TODO Return Mono
    commands.distinctBy { it.name }.forEach { command ->
      val request = command.toRequest()
      val res =
          this.applicationService
              .createGuildApplicationCommand(applicationId, guildId, request)
              .doOnError { err ->
                logger.error(err) { "Could not create guild command $command on guild $guildId" }
              }
              .doOnSuccess {
                logger.info { "Successfully created global command $command on guild $guildId" }
              }
              .block()

      map[res.id().toLong()] = command
    }

    return map
  }

  private fun checkMultipleCommands(commands: Iterable<AbstractCommand>) {
    commands.groupingBy { it.name }.eachCount().filter { it.value > 1 }
        .forEach { (name, count) ->
          logger.warn { "Multiple commands for name $name found ($count times)" }
        }
  }
}
