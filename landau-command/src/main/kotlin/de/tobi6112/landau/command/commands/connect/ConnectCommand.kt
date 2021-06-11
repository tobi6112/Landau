package de.tobi6112.landau.command.commands.connect

import de.tobi6112.landau.command.commands.connect.service.CodewarsServiceClient
import de.tobi6112.landau.command.core.AbstractCommand
import de.tobi6112.landau.command.core.Choice
import de.tobi6112.landau.command.core.Option
import de.tobi6112.landau.command.core.OptionType
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

    val service = getServiceChoice(commandInteraction).block()
    val identifier = getServiceIdentifier(commandInteraction).block()

    if (connectionRepository.isUserAlreadyConnected(userId, service)) {
      return event.reply { it.setContent("${Emoji.WARNING} You are already connected") }
    }
    if (connectionRepository.isIdentifierAlreadyTaken(service, identifier)) {
      return event.reply { it.setContent("${Emoji.WARNING} Identifier is already taken") }
    }

    return this.isValidServiceIdentifier(service, identifier).flatMap { valid ->
      if (valid) {
        Mono.just(ServiceConnection(userId, service, identifier))
            .map { connectionRepository.saveServiceConnection(it) }
            .doOnError { log.error("${Emoji.RED_CROSS} Couldn't save connection", it) }
            .flatMap { _ ->
              event.reply { it.setContent("${Emoji.WHITE_CHECK_MARK} Successfully connected") }
            }
      } else {
        event.reply { reply ->
          reply.setContent("${Emoji.RED_CROSS} Not a valid identifier. Does not exist.")
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
