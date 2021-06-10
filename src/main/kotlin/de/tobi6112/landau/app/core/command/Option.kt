package de.tobi6112.landau.app.core.command

import discord4j.discordjson.json.ApplicationCommandOptionData

/**
 * Command Option
 *
 * @property type type
 * @property name name
 * @property description description
 * @property required required
 * @property choices Only supported when type is INT or STRING
 */
class Option(
    private val type: OptionType,
    private val name: String,
    private val description: String,
    private val required: Boolean = false,
    private val choices: List<Choice> = listOf()
) {
  init {
    require(name.matches("^[\\w-]{1,32}\$".toRegex())) {
      "Command option name must match ^[\\w-]{1,32}\$"
    }
    @Suppress("MAGIC_NUMBER")
    require(description.length in 1..100) {
      "Command option description must contain between 1 and 100 characters"
    }
    @Suppress("MAGIC_NUMBER") require(choices.size <= 25) { "Too many command options" }
  }

  /**
   * Convert to Data schema
   *
   * @return data schema
   */
  fun toData(): ApplicationCommandOptionData {
    val builder =
        ApplicationCommandOptionData.builder()
            .type(type.value)
            .name(name)
            .description(description)
            .required(required)

    when (type) {
      OptionType.INTEGER, OptionType.STRING -> builder.addAllChoices(choices.map { it.toData() })
      else -> {
        // this is a generated else block
      }
    }
    return builder.build()
  }
}
