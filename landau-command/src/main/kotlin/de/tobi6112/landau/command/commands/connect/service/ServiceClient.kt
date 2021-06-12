package de.tobi6112.landau.command.commands.connect.service

import reactor.core.publisher.Mono

/** A client that performs required operations on the service */
interface ServiceClient {
  /**
   * Checks whether the identifier is valid
   *
   * @param identifier identifier
   * @return true if valid
   */
  fun isValidIdentifier(identifier: String): Mono<Boolean>
}
