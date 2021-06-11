package de.tobi6112.landau.data.command

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import javax.xml.stream.events.Namespace

/**
 * Global command repository
 *
 */
interface GlobalCommandRepository {

  /**
   * save global command
   *
   * @param id id of command
   * @param name name of command
   */
  fun saveGlobalCommand(id: Long, name: String)

  /**
   *
   *
   * @param names
   * @return
   */
  fun findCommandIdsByNotInNameList(names: Iterable<String>): Iterable<Long>

  fun existsByName(name: String) : Boolean

  fun findCommandIdByName(name: String): Long?

  fun removeCommandByCommandId(id: Long)
}

class DefaultGlobalCommandRepository : GlobalCommandRepository {
  override fun saveGlobalCommand(id: Long, name: String) {
    return transaction {
      addLogger(Slf4jSqlDebugLogger)

      GlobalCommandTable.insert {
        it[commandId] = id
        it[commandName] = name
      }
    }
  }

  override fun findCommandIdsByNotInNameList(names: Iterable<String>): Iterable<Long> {
      return transaction {
        addLogger(Slf4jSqlDebugLogger)

        GlobalCommandTable.select {
          GlobalCommandTable.commandName notInList names
        }.map {
          it[GlobalCommandTable.commandId]
        }
      }
  }

  override fun existsByName(name: String): Boolean {
    return transaction {
      addLogger(Slf4jSqlDebugLogger)

      GlobalCommandTable.select {
        GlobalCommandTable.commandName eq name
      }.count() > 0L
    }
  }

  override fun findCommandIdByName(name: String): Long? {
    return transaction {
      addLogger(Slf4jSqlDebugLogger)

      GlobalCommandTable.select {
        GlobalCommandTable.commandName eq name
      }.map {
        it[GlobalCommandTable.commandId]
      }.firstOrNull()
    }
  }

  override fun removeCommandByCommandId(id: Long) {
    return transaction {
      addLogger(Slf4jSqlDebugLogger)

      GlobalCommandTable.deleteWhere {
        GlobalCommandTable.commandId eq id
      }
    }
  }
}

