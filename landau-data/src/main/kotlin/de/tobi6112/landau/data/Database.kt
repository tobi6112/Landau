package de.tobi6112.landau.data

import de.tobi6112.landau.data.command.GlobalCommandTable
import de.tobi6112.landau.data.command.GuildCommandTable
import de.tobi6112.landau.data.connect.ServiceConnectionTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object Database {
  fun connect(url: String, driver: String, user: String, password: String) {
    Database.connect(
      url = url,
      driver = driver,
      user = user,
      password = password)

    transaction {
      SchemaUtils.createMissingTablesAndColumns(ServiceConnectionTable, GlobalCommandTable, GuildCommandTable)
    }
  }
}