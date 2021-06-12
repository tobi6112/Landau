/**
 * Contains command exceptions
 */
package de.tobi6112.landau.command.core

/**
 * runtime exception for general command parsing
 *
 * @constructor
 * cause is not required
 *
 * @param message message
 * @param cause cause
 */
open class CommandException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {
  constructor(message: String) : this(message, null)
}

/**
 * The option type is from another type than requested
 *
 * @param message message
 */
class OptionTypeMismatchException(message: String): CommandException(message)

/**
 * Option does not exist or is not parsable
 *
 * @param message message
 */
class InvalidOptionException(message: String): CommandException(message)


