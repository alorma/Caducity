package com.alorma.caducity.ui.screen.transform

import androidx.compose.ui.graphics.Color
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo

class LineContrastTest {
  private val paper = Color(0xFFFFFAEE)
  private val ink = Color(0xFF1C1B16)

  @Test
  fun `a colour with enough contrast is left alone`() {
    val expired = Color(0xFFB23F2A)
    expectThat(expired.withLineContrast(paper)).isEqualTo(expired)
  }

  @Test
  fun `the light yellow is darkened until it reaches 3 to 1`() {
    val soon = Color(0xFFD9A23A)
    val line = soon.withLineContrast(paper)
    expectThat(contrastRatio(line, paper)).isGreaterThanOrEqualTo(MinLineContrast)
  }

  @Test
  fun `every status colour reaches 3 to 1 in light and dark`() {
    listOf(0xFFB23F2A, 0xFFD9A23A, 0xFF7E9B4C, 0xFF4F8FA3).forEach {
      expectThat(contrastRatio(Color(it).withLineContrast(paper), paper)).isGreaterThanOrEqualTo(MinLineContrast)
    }
    listOf(0xFFFF8E78, 0xFFE8B95A, 0xFFA6C277, 0xFF8CC3D4).forEach {
      expectThat(contrastRatio(Color(it).withLineContrast(ink), ink)).isGreaterThanOrEqualTo(MinLineContrast)
    }
  }
}
