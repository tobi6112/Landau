package de.tobi6112.landau.app.misc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class EmojiTest :
  FunSpec({
  test("toString() should return emoji literal") { Emoji.ROCKET.toString() shouldBe ":rocket:" }
})
