package com.alorma.caducity.ui.screen.add

import kotlinx.datetime.LocalDate
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class ExpiryDateParserTest {
  private val feb1 = LocalDate(2026, 2, 1)

  @Test
  fun `parses separated short date`() {
    expectThat(ExpiryDateParser.parse("1/2/26")).isEqualTo(feb1)
    expectThat(ExpiryDateParser.parse("01/02/26")).isEqualTo(feb1)
  }

  @Test
  fun `parses plain digits with 2 or 4 digit year`() {
    expectThat(ExpiryDateParser.parse("010226")).isEqualTo(feb1)
    expectThat(ExpiryDateParser.parse("01022026")).isEqualTo(feb1)
  }

  @Test
  fun `parses pasted dd mm yyyy with other separators`() {
    expectThat(ExpiryDateParser.parse("01/02/2026")).isEqualTo(feb1)
    expectThat(ExpiryDateParser.parse("01-02-2026")).isEqualTo(feb1)
    expectThat(ExpiryDateParser.parse(" 01.02.26 ")).isEqualTo(feb1)
  }

  @Test
  fun `rejects dates that do not exist`() {
    expectThat(ExpiryDateParser.parse("31/02/26")).isNull()
    expectThat(ExpiryDateParser.parse("00/01/26")).isNull()
    expectThat(ExpiryDateParser.parse("01/13/26")).isNull()
  }

  @Test
  fun `accepts leap day only in leap years`() {
    expectThat(ExpiryDateParser.parse("29/02/28")).isEqualTo(LocalDate(2028, 2, 29))
    expectThat(ExpiryDateParser.parse("29/02/27")).isNull()
  }

  @Test
  fun `rejects incomplete or malformed input`() {
    expectThat(ExpiryDateParser.parse("")).isNull()
    expectThat(ExpiryDateParser.parse("01/02")).isNull()
    expectThat(ExpiryDateParser.parse("01/02/2")).isNull()
    expectThat(ExpiryDateParser.parse("01/02/202")).isNull()
    expectThat(ExpiryDateParser.parse("01022")).isNull()
    expectThat(ExpiryDateParser.parse("ab/cd/ef")).isNull()
  }

  @Test
  fun `formats digits inserting slashes progressively`() {
    expectThat(ExpiryDateParser.format("")).isEqualTo("")
    expectThat(ExpiryDateParser.format("01")).isEqualTo("01")
    expectThat(ExpiryDateParser.format("010")).isEqualTo("01/0")
    expectThat(ExpiryDateParser.format("0102")).isEqualTo("01/02")
    expectThat(ExpiryDateParser.format("01022")).isEqualTo("01/02/2")
    expectThat(ExpiryDateParser.format("01022026")).isEqualTo("01/02/2026")
  }

  @Test
  fun `digitsOf strips everything but digits and caps at 8`() {
    expectThat(ExpiryDateParser.digitsOf("01/02/2026")).isEqualTo("01022026")
    expectThat(ExpiryDateParser.digitsOf("0102202699")).isEqualTo("01022026")
    expectThat(ExpiryDateParser.digitsOf("a1b")).isEqualTo("1")
  }

  @Test
  fun `backspace over a slash keeps digits stable`() {
    // Deleting the slash in "01/" yields "01" digits again: format(digitsOf("01")) == "01".
    expectThat(ExpiryDateParser.format(ExpiryDateParser.digitsOf("01"))).isEqualTo("01")
  }
}
