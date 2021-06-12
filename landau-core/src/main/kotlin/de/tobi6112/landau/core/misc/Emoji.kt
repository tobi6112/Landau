package de.tobi6112.landau.core.misc

/**
 * Represents emojis
 *
 * @property notation notation
 */
enum class Emoji(private val notation: String) {
  BOOKMARK("bookmark"),
  TRIANGULAR_FLAG("triangular_flag_on_post"),
  PUSHPIN("pushpin"),
  CROWN("crown"),
  BIRTHDAY("birthday"),
  PEOPLE_HUGGING("people_hugging"),
  GEAR("gear"),
  ROCKET("rocket"),
  PARTYING_FACE("partying_face"),
  RED_CROSS("x"),
  WHITE_CHECK_MARK("white_check_mark"),
  WARNING("warning"),
  ;

  private fun toDiscordNotation() = DiscordLiterals.EMOJI_LITERAL.buildLiteral(this.notation)

  override fun toString() = toDiscordNotation()
}
