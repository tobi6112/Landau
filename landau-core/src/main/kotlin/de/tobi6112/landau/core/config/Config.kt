/** Contains configuration schemas and Configuration related files */

package de.tobi6112.landau.core.config

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
 * Database config schema
 *
 * @property jdbcUrl JDBC URL
 * @property driver Driver class
 * @property username username
 * @property password password
 */
data class DatabaseConfig(
    val jdbcUrl: String,
    val driver: String,
    val username: String,
    val password: String
)

/**
 * Config schema
 *
 * @property bot Bot configuration
 * @property database
 */
data class Config(val bot: BotConfig, val database: DatabaseConfig)

/** Configuration object, used to retrieve configuration file */
object Configuration {
  /**
   * Get configuration
   *
   * @param env environment
   * @return Config
   */
  fun getConfig(env: String?): Config {
    return ConfigLoader.Builder()
        .addSource(EnvironmentVariablesPropertySource(true, allowUppercaseNames = true))
        .addSource(PropertySource.resource("/config-$env.yml"))
        .addSource(PropertySource.resource("/config.yml"))
        .build()
        .loadConfigOrThrow()
  }
}
