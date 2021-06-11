package de.tobi6112.landau

import de.tobi6112.landau.command.service.ApplicationCommandService
import de.tobi6112.landau.core.config.Config
import de.tobi6112.landau.core.config.Configuration
import de.tobi6112.landau.discord.ApplicationInfo
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.rest.service.ApplicationService
import discord4j.rest.util.AllowedMentions
import org.koin.dsl.bind
import org.koin.dsl.module

/** Contains Koin Module */
object LandauModule {
  val module = module {
    single { Configuration.getConfig(getProperty("CONFIG_PROFILE")) }
    single {
      DiscordClient.builder(getProperty("BOT_TOKEN"))
          .setDefaultAllowedMentions(AllowedMentions.suppressEveryone())
          .build()
          .login()
          .block()
    } bind GatewayDiscordClient::class
    single { ApplicationInfo(get()) }
    single {
      val client: GatewayDiscordClient = get()
      return@single client.restClient.applicationService
    } bind ApplicationService::class
    single {
      val applicationInfo: ApplicationInfo = get()
      val config: Config = get()
      return@single ApplicationCommandService(
          applicationId = applicationInfo.applicationId, get(), config.bot.commands)
    } bind ApplicationCommandService::class
  }
}
