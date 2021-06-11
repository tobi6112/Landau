package de.tobi6112.landau.command.commands.connect

import de.tobi6112.landau.command.commands.connect.service.CodewarsServiceClient
import de.tobi6112.landau.command.core.*
import de.tobi6112.landau.core.connect.Service
import de.tobi6112.landau.core.connect.ServiceConnection
import de.tobi6112.landau.core.misc.Emoji
import de.tobi6112.landau.data.connect.DefaultServiceConnectionRepository
import de.tobi6112.landau.data.connect.ServiceConnectionRepository
import discord4j.core.`object`.command.ApplicationCommandInteraction
import discord4j.core.event.domain.InteractionCreateEvent
import mu.KotlinLogging
import reactor.core.publisher.Mono

/**
 * Connection Command
 *
 * @property connectionRepository connection repository
 */
class ConnectCommand(
    private val connectionRepository: ServiceConnectionRepository = DefaultServiceConnectionRepository()
) :
  AbstractCommand(
    name = "connect",
    description = "Connect user with a service",
    options =
        listOf(
            Option(
                type = OptionType.STRING,
                name = "service",
                description = "The service that should be connected",
                required = true,
                choices = Service.values().map { Choice(name = it.key, value = it.name) }),
            Option(
              type = OptionType.STRING,
              name = "identifier",
              description = "The identifier of the service",
              required = true)
        )) {
  private val log = KotlinLogging.logger {}
  private val codewarsService = CodewarsServiceClient()

  override fun handleEvent(event: InteractionCreateEvent): Mono<*> {
    val commandInteraction = event.interaction.commandInteraction
    val userId = getUserId(event)

    return Mono.zip(getServiceChoice(commandInteraction), getServiceIdentifier(commandInteraction))
      .doOnError(CommandException::class.java) { log.error(it) { "Error parsing options" } }
      .flatMap {
        if(connectionRepository.isUserAlreadyConnected(userId, it.t1)) {
          return@flatMap event.reply { reply -> reply.setContent("${Emoji.WARNING} You are already connected") }
        }
        if(connectionRepository.isIdentifierAlreadyTaken(it.t1, it.t2)) {
          return@flatMap event.reply { reply -> reply.setContent("${Emoji.WARNING} Identifier is already taken") }
        }
        return@flatMap this.isValidServiceIdentifier(it.t1, it.t2).flatMap { valid ->
          if (valid) {
            Mono.just(ServiceConnection(userId, it.t1, it.t2))
              .map { serviceCon -> connectionRepository.saveServiceConnection(serviceCon) }
              .doOnError { err -> log.error(err) { "${Emoji.RED_CROSS} Couldn't save connection" } }
              .flatMap { _ ->
                event.reply { reply -> reply.setContent("${Emoji.WHITE_CHECK_MARK} Successfully connected") }
              }
          } else {
            event.reply { reply ->
              reply.setContent("${Emoji.RED_CROSS} Not a valid identifier. Does not exist.")
            }
          }
        }
      }
  }

  private fun getUserId(event: InteractionCreateEvent): Long = event.interaction.user.id.asLong()

  private fun getServiceChoice(interaction: ApplicationCommandInteraction): Mono<Service> {
    return this.getOptionValueFromInteractionAsString(interaction, "service") {
      Service.valueOf(it.uppercase())
    }
  }

  private fun getServiceIdentifier(interaction: ApplicationCommandInteraction): Mono<String> {
    return this.getOptionValueFromInteractionAsString(interaction, "identifier")
  }

  private fun isValidServiceIdentifier(service: Service, identifier: String): Mono<Boolean> {
    return when (service) {
      Service.CODEWARS -> codewarsService.isValidIdentifier(identifier)
    }
  }
}
