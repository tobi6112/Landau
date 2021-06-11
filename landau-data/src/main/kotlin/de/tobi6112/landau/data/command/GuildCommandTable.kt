package de.tobi6112.landau.data.command

import org.jetbrains.exposed.dao.id.UUIDTable

object GuildCommandTable : UUIDTable() {
  val commandId = long("command_id")
  val guildId = long("guild_id")
  val commandName = varchar("command_name", 32)

  init {
    uniqueIndex(guildId, commandId)
    uniqueIndex(guildId, commandName)
  }
}