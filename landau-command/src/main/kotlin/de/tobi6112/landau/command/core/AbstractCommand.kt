package de.tobi6112.landau.command.core

import discord4j.core.`object`.command.ApplicationCommandInteraction
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.event.domain.InteractionCreateEvent
import discord4j.discordjson.json.ApplicationCommandData
import discord4j.discordjson.json.ApplicationCommandRequest
import reactor.core.publisher.Mono

/**
 * Abstract command
 *
 * @constructor notingham
 *
 * @param name name of the command
 * @param description description
 * @property name
 * @property description
 */
abstract class AbstractCommand(
    val name: String,
    private val description: String,
    private val options: List<Option> = listOf()
) {
  init {
    require(name.matches("^[\\w-]{1,32}\$".toRegex())) { "Command name must match ^[\\w-]{1,32}\$" }
    @Suppress("MAGIC_NUMBER")
    require(description.length in 1..100) {
      "Command description must contain between 1 and 100 characters"
    }
  }

  /**
   * Handles a interaction event
   *
   * @param event Interaction event
   * @return ?
   */
  abstract fun handleEvent(event: InteractionCreateEvent): Mono<*>

  /**
   * Convert to request schema
   *
   * @return request schema
   */
  fun toRequest(): ApplicationCommandRequest {
    val builder = ApplicationCommandRequest.builder().name(name).description(description)

    if (options.isNotEmpty()) {
      builder.addAllOptions(options.map { it.toData() })
    }

    return builder.build()
  }

  /**
   * Convert to Data schema
   *
   * @return data schema
   */
  fun toData(applicationId: Long, commandId: Long): ApplicationCommandData {
    val builder = ApplicationCommandData.builder()
      .name(this.name)
      .description(this.description)
      .applicationId(applicationId.toString())
      .id(commandId.toString())
      .defaultPermission(true)
    if(options.isNotEmpty()) {
      builder.addAllOptions(options.map { it.toData() })
    }
    return builder.build()
  }

  /**
   * Get command option value
   *
   * @param interaction interaction
   * @param name name
   * @return Tuple with Type and value
   * @throws InvalidOptionException if option does not exist
   */
  private fun getOptionValueFromInteraction(interaction: ApplicationCommandInteraction,
                                             name: String): Mono<Pair<OptionType, ApplicationCommandInteractionOptionValue>> {
    return Mono.just(interaction.getOption(name))
      .filter { it.isPresent }
      .map { it.get() }
      .flatMap {
        val type = OptionType.fromApplicationCommandOptionType(it.type)
        val value = it.value
        if(value.isEmpty) {
          return@flatMap Mono.error(InvalidOptionException("Option $name does not exist"))
        }
        return@flatMap Mono.just(Pair(type, value.get()))
      }
  }

  /**
   * Get named option value as string
   *
   * @param interaction interaction
   * @param name name of the option
   * @return value or empty if no value present
   */
  fun getOptionValueFromInteractionAsString(interaction: ApplicationCommandInteraction, name: String) : Mono<String> {
      return this.getOptionValueFromInteractionAsString(interaction, name) { it }
  }

  /**
   * Get named option value as string
   *
   * @param interaction interaction
   * @param name name of the option
   * @param transform mapping function to transform string result
   * @return value or empty if no value present
   * @throws OptionTypeMismatchException if option type is not string
   */
  fun <T> getOptionValueFromInteractionAsString(interaction: ApplicationCommandInteraction, name: String, transform: (String) -> T) : Mono<T> {
    return this.getOptionValueFromInteraction(interaction, name)
      .flatMap {
        when(it.first) {
          OptionType.STRING -> Mono.just(it.second.asString())
          else -> Mono.error(OptionTypeMismatchException("Option not applicable as String"))
        }
      }.map { transform(it) }
  }
}
