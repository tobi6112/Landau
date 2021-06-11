package de.tobi6112.landau.command.service

import de.tobi6112.landau.command.core.AbstractCommand
import de.tobi6112.landau.core.config.CommandsConfig
import de.tobi6112.landau.data.command.GlobalCommandRepository
import de.tobi6112.landau.data.command.GuildCommandRepository
import discord4j.discordjson.json.ApplicationCommandData
import discord4j.rest.service.ApplicationService
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Application command service
 *
 * @property applicationId application id
 * @property applicationService application service
 * @property config config
 */
class ApplicationCommandService(
    private val applicationId: Long,
    private val applicationService: ApplicationService,
    private val config: CommandsConfig,
    private val globalCommandRepository: GlobalCommandRepository,
    private val guildCommandRepository: GuildCommandRepository
) {
  private val logger = KotlinLogging.logger {}

  /**
   * Processes global commands like in the following schema:
   *
   * - Step 1: Get active global commands
   * - Step 2: Check if command ID is in Database
   *    - Step 2.1: If command does not exist -> Create Command and save ID in database
   *    - Step 2.2: If command does exist -> Get Command and check whether it needs an update
   *       - Step 2.2.1: If it needs an update -> patch command
   * - Step 3: Get dangling (name not in active command) command IDs from Database
   * - Step 4: Delete dangling commands
   *
   * @param activeCommands active commands
   */
  fun processGlobalCommands(activeCommands: Iterable<AbstractCommand>) {
    Flux.fromIterable(activeCommands)
      .filterWhen { isGlobalCommandEnabled(it) }
      .flatMap {
        if(this.globalCommandRepository.existsByName(it.name)) {
          return@flatMap this.updateGlobalCommandIfNecessary(it)
        } else {
          return@flatMap this.createGlobalCommandReactive(it)
        }
      }
      .subscribe()

    Mono.just(activeCommands)
      .map { this.globalCommandRepository.findCommandIdsByNotInNameList(it.map { it.name }) }
      .map { this.removeGlobalCommands(it) }
      .subscribe()
  }

  /**
   * Processes global commands like in the following schema:
   *
   * - Step 1: Get active commands
   * - Step 2: For each guild that is in config
   * - Step 3: Check if command is enabled in guild
   * - Step 4: Check if command ID is in Database
   *    - Step 4.1: If command does not exist -> Create Command and save ID in database
   *    - Step 4.2: If command does exist -> Get Command and check whether it needs an update
   *       - Step 4.2.1: If it needs an update -> patch command
   * - Step 5: Get dangling (name not in active command) command IDs from Database
   * - Step 6: Delete dangling commands
   *
   * @param activeCommands active commands
   */
  fun processGuildCommands(activeCommands: Iterable<AbstractCommand>) {
    config.guilds.keys.forEach {
      processGuildCommands(it, activeCommands)
    }
  }

  private fun isGlobalCommandEnabled(command: AbstractCommand) =
    Mono.just(config.global[command.name]?.enabled ?: true)

  private fun processGuildCommands(guildId: Long, activeCommands: Iterable<AbstractCommand>) {
    Flux.fromIterable(activeCommands)
      .filterWhen { this.isGuildCommandEnabled(guildId, it) }
      .flatMap {
        if(this.guildCommandRepository.existsByName(guildId, it.name)) {
          return@flatMap this.updateGuildCommandIfNecessary(guildId, it)
        } else {
          return@flatMap this.createGuildCommandReactive(guildId, it)
        }
      }
      .subscribe()

    Mono.just(activeCommands)
      .map { this.guildCommandRepository.findCommandIdsByNotInNameList(guildId, it.map { it.name }) }
      .map { this.removeCommandsFromGuild(guildId, it) }
      .subscribe()
  }

  private fun updateGuildCommandIfNecessary(guildId: Long, command: AbstractCommand) : Mono<ApplicationCommandData> {
    return Mono.just(command)
      .zipWhen {
        val id = this.guildCommandRepository.findCommandIdByName(guildId, command.name)
          ?: throw RuntimeException("Guild Command ${command.name} was not found in database")
        getGuildCommand(guildId, id)
      }.filter {
        /*val data = it.t1.toData(applicationId, it.t2.id().toLong())
        needsUpdate(data, it.t2)*/
        //TODO Check if update is required
        false
      }
      .doOnNext { logger.debug { "Update for guild command ${command.name} required" } }
      .flatMap {
        val id = this.guildCommandRepository.findCommandIdByName(guildId, it.t1.name)!!
        return@flatMap this.updateGuildCommand(guildId, id, it.t1)
      }
  }

  private fun getGuildCommand(guildId: Long, commandId: Long): Mono<ApplicationCommandData> {
    return this.applicationService.getGuildApplicationCommand(applicationId, guildId, commandId)
      .doOnSuccess { logger.debug { "Retrieved guild application command with id $commandId from guild $guildId" } }
      .doOnError { logger.warn(it) { "Could not retrieve guild application command with id $commandId from guild $guildId" } }
  }

  private fun updateGlobalCommandIfNecessary(command: AbstractCommand): Mono<ApplicationCommandData> {
    return Mono.just(command)
      .zipWhen {
        val id = this.globalCommandRepository.findCommandIdByName(it.name)
          ?: throw RuntimeException("Command ${command.name} was not found in database")
        getGlobalCommand(id)
      }.filter {
        /*val data = it.t1.toData(applicationId, it.t2.id().toLong())
        needsUpdate(data, it.t2)*/
        //TODO Check if update is required
        false
      }
      .doOnNext { logger.debug { "Update for command ${command.name} required" } }
      .flatMap {
        val id = this.globalCommandRepository.findCommandIdByName(it.t1.name)!!
        return@flatMap this.updateGlobalCommand(id, it.t1)
      }
  }

  private fun updateGlobalCommand(commandId: Long, command: AbstractCommand): Mono<ApplicationCommandData> {
    return this.applicationService.modifyGlobalApplicationCommand(applicationId, commandId, command.toRequest())
      .doOnError { logger.error(it) { "Could not update command with id $commandId (name: ${command.name})" } }
      .doOnSuccess { logger.debug { "Successfully updated command with id $commandId (name: ${command.name})" } }
  }

  private fun updateGuildCommand(guildId: Long, commandId: Long, command: AbstractCommand): Mono<ApplicationCommandData> {
    return this.applicationService.modifyGuildApplicationCommand(applicationId, guildId, commandId, command.toRequest())
      .doOnError { logger.error(it) { "Could not update command with id $commandId (name: ${command.name}) on guild $guildId" } }
      .doOnSuccess { logger.debug { "Successfully updated command with id $commandId (name: ${command.name}) on guild $guildId" } }
  }

  private fun getGlobalCommand(commandId: Long): Mono<ApplicationCommandData> {
    return this.applicationService.getGlobalApplicationCommand(applicationId, commandId)
      .doOnSuccess { logger.debug { "Retrieved global application command with id $commandId" } }
      .doOnError { logger.warn(it) { "Could not retrieve global application command with id $commandId" } }
  }

  private fun removeGlobalCommands(commandIds: Iterable<Long>): Flux<Long> {
    return Flux.fromIterable(commandIds)
      .doOnNext {
        applicationService.deleteGlobalApplicationCommand(applicationId, it)
        logger.debug { "Successfully removed command with id $it" }
      }
      .doOnError { logger.error(it) { "Could not delete global command" } }
      .doOnNext { this.globalCommandRepository.removeCommandByCommandId(it) }
      .doOnError { logger.error(it) { "Could not delete global command from database" } }
  }

  private fun createGlobalCommandReactive(command: AbstractCommand) =
      this.applicationService
          .createGlobalApplicationCommand(applicationId, command.toRequest())
          .doOnSuccess { logger.info { "Successfully created global command $command" } }
          .doOnError { err -> logger.error(err) { "Could not create global command $command" } }
          .doOnSuccess { this.globalCommandRepository.saveGlobalCommand(it.id().toLong(), it.name()) }
          .doOnError { err -> logger.error(err) { "Could not save global command $command" } }

  private fun createGuildCommandsReactive(commands: Iterable<AbstractCommand>) =
      Flux.fromIterable(config.guilds.entries).flatMap { map ->
        this.createGuildCommandsReactive(
            map.key, commands.filter { map.value.keys.contains(it.name) })
      }

  private fun createGuildCommandsReactive(guildId: Long, commands: Iterable<AbstractCommand>) =
      Flux.fromIterable(commands)
          .filterWhen { isGuildCommandEnabled(guildId, it) }
          .filter { !this.guildCommandRepository.existsByName(guildId, it.name) }
          .flatMap { this.createGuildCommandReactive(guildId, it) }
          .map { cmd -> Pair(cmd.id().toLong(), commands.find { cmd.name() == it.name }!!) }
          .doOnNext { pair -> guildCommandRepository.saveGuildCommand(pair.first, guildId, pair.second.name) }

  private fun isGuildCommandEnabled(guildId: Long, command: AbstractCommand) =
      Mono.just(config.guilds[guildId]?.get(command.name)?.enabled ?: false)

  private fun createGuildCommandReactive(guildId: Long, command: AbstractCommand) =
      this.applicationService
          .createGuildApplicationCommand(applicationId, guildId, command.toRequest())
          .filterWhen { isGuildCommandEnabled(guildId, command) }
          .doOnSuccess {
            logger.info { "Successfully created guild command $command on guild $guildId" }
          }
          .doOnError { err ->
            logger.error(err) { "Could not create guild command $command on guild $guildId" }
          }

  private fun removeCommandsFromGuild(guildId: Long, commandIds: Iterable<Long>): Flux<Long> {
    return Flux.fromIterable(commandIds)
      .doOnNext {
        applicationService.deleteGuildApplicationCommand(applicationId, guildId, it)
        logger.debug { "Successfully removed command with id $it" }
      }
      .doOnError { logger.error(it) { "Could not delete global command" } }
      .doOnNext { this.guildCommandRepository.removeCommandByCommandId(guildId, it) }
      .doOnError { logger.error(it) { "Could not delete global command from database" } }
  }
}
