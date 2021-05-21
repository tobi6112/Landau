/**
 * Contains configuration schemas and Configuration related files
 */

package de.tobi6112.landau.config

import com.sksamuel.hoplite.ConfigLoader

/**
 * Command config schema
 *
 * @property prefix prefix used for comamnds
 */
data class Command(val prefix: String)

/**
 * Bot config schema
 *
 * @property command Command configuration
 */
data class Bot(val command: Command)

/**
 * Config schema
 *
 * @property bot Bot configuration
 */
data class Config(val bot: Bot)

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
    return ConfigLoader().loadConfigOrThrow(file)
  }
}
