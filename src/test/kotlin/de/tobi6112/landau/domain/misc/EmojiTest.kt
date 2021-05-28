package de.tobi6112.landau.domain.misc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*

internal class EmojiTest :
  FunSpec({
  test("toString() should return emoji literal") { Emoji.ROCKET.toString() shouldBe ":rocket:" }
})
