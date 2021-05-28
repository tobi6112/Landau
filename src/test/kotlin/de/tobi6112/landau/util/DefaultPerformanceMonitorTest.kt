package de.tobi6112.landau.util

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeExactly
import org.mockito.kotlin.*

import javax.management.MBeanServer

internal class DefaultPerformanceMonitorTest :
  ShouldSpec({
  context("CPU usage") {
    should("return cpu usage") {
      // Given
      val mbs: MBeanServer =
          mock<MBeanServer> { on { getAttribute(any(), eq("ProcessCpuLoad")) } doReturn 4.5 }
      val monitor =
          DefaultPerformanceMonitor(mbs).apply {
            // When
            cpuUsage() shouldBeExactly 4.5
          }

      verify(mbs).getAttribute(any(), eq("ProcessCpuLoad"))
    }
    should("return zero") {
      // Given
      val mbs: MBeanServer =
          mock<MBeanServer> { on { getAttribute(any(), eq("ProcessCpuLoad")) } doReturn null }
      val monitor =
          DefaultPerformanceMonitor(mbs).apply {
            // When
            cpuUsage() shouldBeExactly 0.0
          }

      verify(mbs).getAttribute(any(), eq("ProcessCpuLoad"))
    }
  }
})
