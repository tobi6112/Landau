/** Contains entity and database schema */

package de.tobi6112.landau.app.connect

import de.tobi6112.landau.app.core.data.AbstractEntity
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

/**
 * Service Connection definition
 *
 * @property discordId Discord ID
 * @property service Service
 * @property serviceIdentifier Identifier
 */
class ServiceConnection(val discordId: Long, val service: Service, val serviceIdentifier: String) :
  AbstractEntity()
