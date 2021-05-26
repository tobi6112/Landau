package de.tobi6112.landau.domain.misc

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
  ;

  private fun toDiscordNotation() = ":$notation:"

  override fun toString() = toDiscordNotation()
}
