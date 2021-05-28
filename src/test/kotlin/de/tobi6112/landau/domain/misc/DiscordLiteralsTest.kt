package de.tobi6112.landau.domain.misc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class DiscordLiteralsTest :
  FunSpec({
  test("Should create emoji literal") {
    DiscordLiterals.EMOJI_LITERAL.buildLiteral("rocket") shouldBe ":rocket:"
  }
  test("Should create mention literal") {
    DiscordLiterals.MENTION_LITERAL.buildLiteral("1325525356735") shouldBe "<@1325525356735>"
  }
})
