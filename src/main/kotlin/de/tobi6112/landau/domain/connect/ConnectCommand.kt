package de.tobi6112.landau.domain.connect

import de.tobi6112.landau.domain.core.command.AbstractCommand
import de.tobi6112.landau.domain.core.command.Choice
import de.tobi6112.landau.domain.core.command.Option
import de.tobi6112.landau.domain.core.command.OptionType
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

  override fun handleEvent(event: InteractionCreateEvent): Mono<*> {
    val userId = getUserId(event)
    val service = getServiceChoice(event)
    val identifier = getServiceIdentifier(event)

    if (connectionRepository.isUserAlreadyConnected(userId, service)) {
      return event.reply { it.setContent("You are already connected") }
    }
    if (connectionRepository.isIdentifierAlreadyTaken(service, identifier)) {
      return event.reply { it.setContent("Identifier is already taken") }
    }

    return Mono.just(ServiceConnection(userId, service, identifier))
        .map { connectionRepository.saveServiceConnection(it) }
        .doOnError { log.error("Couldn't save connection", it) }
        .flatMap { _ -> event.reply { it.setContent("Successfully connected") } }
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
}
