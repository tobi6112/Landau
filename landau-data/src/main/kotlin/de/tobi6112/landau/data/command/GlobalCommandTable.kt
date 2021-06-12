package de.tobi6112.landau.data.command

import org.jetbrains.exposed.dao.id.UUIDTable

/**
 * Schema for global commands
 */
object GlobalCommandTable : UUIDTable() {
  val commandId = long("command_id").uniqueIndex()
  val commandName = varchar("command_name", 32).uniqueIndex()
}