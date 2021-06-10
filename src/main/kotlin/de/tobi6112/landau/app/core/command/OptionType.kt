package de.tobi6112.landau.app.core.command

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
}
