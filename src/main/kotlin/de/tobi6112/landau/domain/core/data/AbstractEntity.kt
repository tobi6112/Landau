package de.tobi6112.landau.domain.core.data

import java.util.*

/**
 * Abstract entity
 *
 * @property id read-only random UUID
 */
abstract class AbstractEntity(val id: UUID = UUID.randomUUID()) {
}
