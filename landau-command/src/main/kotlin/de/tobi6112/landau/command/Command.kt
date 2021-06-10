package de.tobi6112.landau.command

import de.tobi6112.landau.command.commands.connect.ConnectCommand
import de.tobi6112.landau.command.commands.info.InfoCommand
import de.tobi6112.landau.command.core.AbstractCommand

object Command {
  fun getAllCommands(): Iterable<AbstractCommand> {
    return setOf(InfoCommand(), ConnectCommand())
  }
}