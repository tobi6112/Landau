package de.tobi6112.landau.command

import de.tobi6112.landau.command.core.AbstractCommand
import de.tobi6112.landau.command.core.Choice
import de.tobi6112.landau.command.core.Option
import de.tobi6112.landau.command.core.OptionType

/** Information command */
class InfoCommand :
  AbstractCommand(
    name = "info",
    description = "Retrieve information",
    options =
        listOf(
            Option(
              type = OptionType.STRING,
              name = "subject",
              description = "The subject to retrieve information about",
              required = true,
              choices =
                  listOf(
                      Choice("bot", "Information about the bot"),
                      Choice("server", "Information about the server"))))) {
}
