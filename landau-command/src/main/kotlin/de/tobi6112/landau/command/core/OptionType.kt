package de.tobi6112.landau.command.core

import discord4j.rest.util.ApplicationCommandOptionType

/** @property value */
@Suppress("MAGIC_NUMBER")
enum class OptionType(val value: Int) {
  // Ignore Subcommands for now as it brings more complexity
  // SUB_COMMAND(1),
  // SUB_COMMAND_GROUP(2),
  STRING(3),
  INTEGER(4),
  BOOLEAN(5),
  USER(6),
  CHANNEL(7),
  ROLE(8),
  MENTIONABLE(9),
  ;

  companion object {
    fun fromApplicationCommandOptionType(optionType: ApplicationCommandOptionType): OptionType {
      return OptionType.values().find { it.value == optionType.value }
        ?: throw RuntimeException("No OptionType for value ${optionType.value} present")
    }
  }
}
