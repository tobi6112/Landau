package de.tobi6112.landau.command

import discord4j.core.event.domain.message.MessageCreateEvent

/**
 * Command
 *
 * @constructor Nothing
 *
 * @param command Command name
 */
abstract class Command(command: String) {
  /**
   * Run command
   *
   * @param event event
   */
  abstract fun run(event: MessageCreateEvent)
}
