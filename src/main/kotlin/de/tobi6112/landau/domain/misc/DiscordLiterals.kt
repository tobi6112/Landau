package de.tobi6112.landau.domain.misc

/**
 * Discord literals
 *
 * @property format format that is used to build the literal
 */
enum class DiscordLiterals(private val format: String) {
  EMOJI_LITERAL(":%s:"),
  MENTION_LITERAL("<@%s>"),
  ;

  /**
   * Build the literal
   *
   * @param str string used to apply format
   * @return formatted literal
   */
  fun buildLiteral(str: String) = this.format.format(str)
}
