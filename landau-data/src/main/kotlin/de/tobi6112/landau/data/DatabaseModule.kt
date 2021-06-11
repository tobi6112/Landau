package de.tobi6112.landau.data

import de.tobi6112.landau.data.command.DefaultGlobalCommandRepository
import de.tobi6112.landau.data.command.DefaultGuildCommandRepository
import de.tobi6112.landau.data.command.GlobalCommandRepository
import de.tobi6112.landau.data.command.GuildCommandRepository
import de.tobi6112.landau.data.connect.DefaultServiceConnectionRepository
import de.tobi6112.landau.data.connect.ServiceConnectionRepository
import org.koin.dsl.bind
import org.koin.dsl.module

object DatabaseModule {
  val module = module {
    single { DefaultServiceConnectionRepository() } bind ServiceConnectionRepository::class
    single { DefaultGlobalCommandRepository() } bind GlobalCommandRepository::class
    single { DefaultGuildCommandRepository() } bind GuildCommandRepository::class
  }
}