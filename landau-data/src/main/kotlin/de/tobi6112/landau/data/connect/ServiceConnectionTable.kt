package de.tobi6112.landau.data.connect

import de.tobi6112.landau.core.connect.Service
import org.jetbrains.exposed.dao.id.UUIDTable

/** Table schema for a service connection */
object ServiceConnectionTable : UUIDTable() {
  val discordId = long("discord_id")
  val service = enumeration("service", Service::class)
  val serviceIdentifier = varchar("service_identifier", length = 255)

  init {
    uniqueIndex(service, serviceIdentifier)
    uniqueIndex(discordId, service)
  }
}