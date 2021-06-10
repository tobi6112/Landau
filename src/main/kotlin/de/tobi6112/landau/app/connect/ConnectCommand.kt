package de.tobi6112.landau.app.connect

import de.tobi6112.landau.app.connect.service.CodewarsServiceClient
import de.tobi6112.landau.app.core.command.AbstractCommand
import de.tobi6112.landau.app.core.command.Choice
import de.tobi6112.landau.app.core.command.Option
import de.tobi6112.landau.app.core.command.OptionType
import de.tobi6112.landau.app.misc.Emoji
import discord4j.core.event.domain.InteractionCreateEvent
import mu.KotlinLogging
import reactor.core.publisher.Mono

/**
 * Connection Command
 *
 * @property connectionRepository connection repository
 */
class ConnectCommand(
    private val connectionRepository: ConnectionRepository = DefaultConnectionRepository
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
              required = true))) {
  private val log = KotlinLogging.logger {}
  private val codewarsService = CodewarsServiceClient()

  override fun handleEvent(event: InteractionCreateEvent): Mono<*> {
    val userId = getUserId(event)
    val service = getServiceChoice(event)
    val identifier = getServiceIdentifier(event)

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

  private fun getServiceChoice(event: InteractionCreateEvent): Service {
    return Service.valueOf(
        event
            .interaction
            .commandInteraction
            .getOption("service")
            .orElseThrow()
            .value
            .orElseThrow()
            .asString()
            .uppercase())
  }

  private fun getServiceIdentifier(event: InteractionCreateEvent): String {
    return event
        .interaction
        .commandInteraction
        .getOption("identifier")
        .orElseThrow()
        .value
        .orElseThrow()
        .asString()
  }

  private fun isValidServiceIdentifier(service: Service, identifier: String): Mono<Boolean> {
    return when (service) {
      Service.CODEWARS -> codewarsService.isValidIdentifier(identifier)
    }
  }
}
