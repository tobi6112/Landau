package de.tobi6112.landau.command.service

import de.tobi6112.landau.command.core.AbstractCommand
import de.tobi6112.landau.core.config.CommandsConfig
import discord4j.core.`object`.entity.ApplicationInfo
import discord4j.rest.service.ApplicationService
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Application command service
 *
 * @property applicationInfo application info
 * @property applicationService application service
 */
class ApplicationCommandService(
    private val applicationInfo: ApplicationInfo,
    private val applicationService: ApplicationService,
    private val config: CommandsConfig
) {
  private val logger = KotlinLogging.logger {}
  private val applicationId = applicationInfo.id.asLong()

  /**
   * Create commands and return their associated ID as well as the command
   *
   * @param commands iterable of commands to create
   * @return reactive tuples of their associated id and the command
   */
  fun createCommands(commands: Iterable<AbstractCommand>) =
      Flux.merge(
          this.createGlobalCommandsReactive(commands), this.createGuildCommandsReactive(commands))

  private fun createGlobalCommandsReactive(commands: Iterable<AbstractCommand>) =
      Flux.fromIterable(commands)
          .filterWhen { isGlobalCommandEnabled(it) }
          .flatMap { this.createGlobalCommandReactive(it) }
          .map { cmd -> Pair(cmd.id().toLong(), commands.find { cmd.name() == it.name }!!) }

  private fun isGlobalCommandEnabled(command: AbstractCommand) =
      Mono.just(config.global[command.name]?.enabled ?: true)

  private fun createGlobalCommandReactive(command: AbstractCommand) =
      this.applicationService
          .createGlobalApplicationCommand(applicationId, command.toRequest())
          .doOnSuccess { logger.info { "Successfully created global command $command" } }
          .doOnError { err -> logger.error(err) { "Could not create global command $command" } }

  private fun createGuildCommandsReactive(commands: Iterable<AbstractCommand>) =
      Flux.fromIterable(config.guilds.entries).flatMap { map ->
        this.createGuildCommandsReactive(
            map.key, commands.filter { map.value.keys.contains(it.name) })
      }

  private fun createGuildCommandsReactive(guildId: Long, commands: Iterable<AbstractCommand>) =
      Flux.fromIterable(commands)
          .filterWhen { isGuildCommandEnabled(guildId, it) }
          .flatMap { this.createGuildCommandReactive(guildId, it) }
          .map { cmd -> Pair(cmd.id().toLong(), commands.find { cmd.name() == it.name }!!) }

  private fun isGuildCommandEnabled(guildId: Long, command: AbstractCommand) =
      Mono.just(config.guilds?.get(guildId)?.get(command.name)?.enabled ?: false)

  private fun createGuildCommandReactive(guildId: Long, command: AbstractCommand) =
      this.applicationService
          .createGuildApplicationCommand(applicationId, guildId, command.toRequest())
          .filterWhen { isGuildCommandEnabled(guildId, command) }
          .doOnSuccess {
            logger.info { "Successfully created global command $command on guild $guildId" }
          }
          .doOnError { err ->
            logger.error(err) { "Could not create guild command $command on guild $guildId" }
          }
}
