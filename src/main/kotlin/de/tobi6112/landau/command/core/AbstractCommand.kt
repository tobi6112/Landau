package de.tobi6112.landau.command.core

import discord4j.discordjson.json.ApplicationCommandRequest

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
open class AbstractCommand(
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
   * Convert to Data schema
   *
   * @return data schema
   */
  fun toRequest(): ApplicationCommandRequest {
    val builder = ApplicationCommandRequest.builder().name(name).description(description)

    if (options.isNotEmpty()) {
      builder.addAllOptions(options.map { it.toData() })
    }

    return builder.build()
  }
}
