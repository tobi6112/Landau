package de.tobi6112.landau.command

import de.tobi6112.landau.command.commands.connect.ConnectCommand
import de.tobi6112.landau.command.commands.info.InfoCommand
import de.tobi6112.landau.command.core.AbstractCommand
import org.koin.dsl.bind
import org.koin.dsl.module

object CommandModule {
  val module = module {
    single { InfoCommand() } bind AbstractCommand::class
    single { ConnectCommand() } bind AbstractCommand::class
  }
}