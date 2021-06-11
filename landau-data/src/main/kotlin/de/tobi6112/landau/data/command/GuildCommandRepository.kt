package de.tobi6112.landau.data.command

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Guild command repository
 *
 */
interface GuildCommandRepository {

  /**
   * save guild command
   *
   * @param commandId id of command
   * @param guildId id of guild
   * @param name name of command
   */
  fun saveGuildCommand(commandId: Long, guildId: Long, name: String)

  fun existsByName(guildId: Long, name: String): Boolean

  fun findCommandIdByName(guildId: Long, name: String): Long?

  fun findCommandIdsByNotInNameList(guildId: Long, names: Iterable<String>): Iterable<Long>

  fun removeCommandByCommandId(guildId: Long, id: Long)
}

class DefaultGuildCommandRepository : GuildCommandRepository {
  override fun saveGuildCommand(cmdId: Long, gldId: Long, name: String) {
    return transaction {
      addLogger(Slf4jSqlDebugLogger)

      GuildCommandTable.insert {
        it[commandId] = cmdId
        it[commandName] = name
        it[guildId] = gldId
      }
    }
  }

  override fun existsByName(guildId: Long, name: String): Boolean {
    return transaction {
      addLogger(Slf4jSqlDebugLogger)

      GuildCommandTable.select {
        (GuildCommandTable.commandName eq name) and (GuildCommandTable.guildId eq guildId)
      }.count() > 0L
    }
  }

  override fun findCommandIdByName(guildId: Long, name: String): Long? {
    return transaction {
      addLogger(Slf4jSqlDebugLogger)

      GuildCommandTable.select {
        (GuildCommandTable.commandName eq name) and (GuildCommandTable.guildId eq guildId)
      }.map {
        it[GuildCommandTable.commandId]
      }.firstOrNull()
    }
  }

  override fun findCommandIdsByNotInNameList(
    guildId: Long,
    names: Iterable<String>
  ): Iterable<Long> {
    return transaction {
      addLogger(Slf4jSqlDebugLogger)

      GuildCommandTable.select {
        (GuildCommandTable.guildId eq guildId) and (GuildCommandTable.commandName notInList names)
      }.map {
        it[GuildCommandTable.commandId]
      }
    }
  }

  override fun removeCommandByCommandId(guildId: Long, id: Long) {
    return transaction {
      addLogger(Slf4jSqlDebugLogger)

      GuildCommandTable.deleteWhere {
        (GuildCommandTable.guildId eq guildId) and (GuildCommandTable.commandId eq id)
      }
    }
  }
}

