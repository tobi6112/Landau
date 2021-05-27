package de.tobi6112.landau.util

import mu.KotlinLogging

import java.lang.management.ManagementFactory
import javax.management.ObjectName

/** Default performance monitor */
class DefaultPerformanceMonitor : PerformanceMonitor {
  private val logger = KotlinLogging.logger {}
  private val mbs = ManagementFactory.getPlatformMBeanServer()
  private val runtime = Runtime.getRuntime()

  override fun usedMemory(): Long = this.availableMemory() - this.runtime.freeMemory()

  override fun availableMemory(): Long = this.runtime.totalMemory()

  override fun cpuUsage(): Double {
    val name = ObjectName.getInstance("java.lang:type=OperatingSystem")

    val load = mbs.getAttribute(name, "ProcessCpuLoad")
    if (load is Double) {
      return load
    }
    logger.warn { "Could not get CPU usage, value is '$load'" }
    return 0.0
  }

  override fun availableProcessors(): Int = Runtime.getRuntime().availableProcessors()
}
