/**
 * Contains configuration schemas and Configuration related files
 */

package de.tobi6112.landau.config

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.EnvironmentVariablesPropertySource
import com.sksamuel.hoplite.PropertySource

typealias GuildsCommandConfig = Map<Long, Map<String, CommandConfig>>
typealias GlobalCommandsConfig = Map<String, CommandConfig>

/**
 * Command config schema
 *
 * @property enabled command enabled
 */
data class CommandConfig(val enabled: Boolean = true)

/**
 * Commands config schema
 *
 * @property global global commands
 * @property guilds guild commands
 */
data class CommandsConfig(val global: GlobalCommandsConfig, val guilds: GuildsCommandConfig)

/**
 * Bot config schema
 *
 * @property commands Commands configuration
 */
data class BotConfig(val commands: CommandsConfig)

/**
 * Config schema
 *
 * @property bot Bot configuration
 */
data class Config(val bot: BotConfig)

/**
 * Configuration object, used to retrieve configuration file
 */
object Configuration {
  /**
   * Get configuration
   *
   * @param env environment
   * @return Config
   */
  fun getConfig(env: String?): Config {
    val file = env?.let {
      "/config-$env.yml"
    }
        ?: run {
          "/config.yml"
        }
    return ConfigLoader.Builder()
        .addSource(EnvironmentVariablesPropertySource(true, allowUppercaseNames = true))
        .addSource(PropertySource.resource(file))
        .build()
        .loadConfigOrThrow()
  }
}
