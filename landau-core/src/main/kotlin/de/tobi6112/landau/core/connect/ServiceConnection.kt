/** Contains entity and database schema */

package de.tobi6112.landau.core.connect


/**
 * Service Connection definition
 *
 * @property discordId Discord ID
 * @property service Service
 * @property serviceIdentifier Identifier
 */
class ServiceConnection(val discordId: Long, val service: Service, val serviceIdentifier: String)
