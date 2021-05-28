package de.tobi6112.landau.util

/** Performance Monitor that provides performance metrics */
interface PerformanceMonitor {
  /**
   * Get used memory
   *
   * @return memory in bytes
   */
  fun usedMemory(): Long

  /**
   * Get available memory
   *
   * @return memory in bytes
   */
  fun availableMemory(): Long

  /**
   * Get cpu load
   *
   * @return cpu usage in percent
   */
  fun cpuUsage(): Double

  /**
   * Number of processors available
   *
   * @return number of processors
   */
  fun availableProcessors(): Int
}
