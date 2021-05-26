package de.tobi6112.landau.domain.command.core

import discord4j.discordjson.json.ApplicationCommandOptionChoiceData

/**
 * Choice
 *
 * @property name name
 * @property value value
 */
class Choice(private val name: String, private val value: String) {
  init {
    @Suppress("MAGIC_NUMBER")
    require(name.length in 1..100) { "Choice name must contain between 1 and 100 characters" }
    @Suppress("MAGIC_NUMBER")
    require(value.length in 1..100) { "Choice value must contain between 1 and 100 characters" }
  }

  /**
   * Convert to Data schema
   *
   * @return data schema
   */
  fun toData(): ApplicationCommandOptionChoiceData =
      ApplicationCommandOptionChoiceData.builder().name(name).value(value)
          .build()
}
