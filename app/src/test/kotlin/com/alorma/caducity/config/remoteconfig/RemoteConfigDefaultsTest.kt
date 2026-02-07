package com.alorma.caducity.config.remoteconfig

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsKey
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty

/**
 * Tests for RemoteConfigDefaults to ensure proper structure and values.
 */
class RemoteConfigDefaultsTest {

  @Test
  fun `defaults map contains all defined keys`() {
    // Verify all keys have corresponding default values
    expectThat(RemoteConfigDefaults.defaults) {
      containsKey(RemoteConfigDefaults.Keys.EXAMPLE_FEATURE_ENABLED)
      containsKey(RemoteConfigDefaults.Keys.EXAMPLE_MESSAGE)
      containsKey(RemoteConfigDefaults.Keys.EXAMPLE_NUMBER)
    }
  }

  @Test
  fun `defaults map has expected size`() {
    // If you add new keys, update this test
    expectThat(RemoteConfigDefaults.defaults).hasSize(3)
  }

  @Test
  fun `example feature enabled has boolean default value`() {
    val value = RemoteConfigDefaults.defaults[RemoteConfigDefaults.Keys.EXAMPLE_FEATURE_ENABLED]
    expectThat(value).isEqualTo(false)
  }

  @Test
  fun `example message has non-empty string default value`() {
    val value = RemoteConfigDefaults.defaults[RemoteConfigDefaults.Keys.EXAMPLE_MESSAGE] as String
    expectThat(value).isNotEmpty()
  }

  @Test
  fun `example number has positive long default value`() {
    val value = RemoteConfigDefaults.defaults[RemoteConfigDefaults.Keys.EXAMPLE_NUMBER] as Long
    expectThat(value).isEqualTo(42L)
  }

  @Test
  fun `keys object contains expected constants`() {
    // Verify key constants are defined
    expectThat(RemoteConfigDefaults.Keys.EXAMPLE_FEATURE_ENABLED).isEqualTo("example_feature_enabled")
    expectThat(RemoteConfigDefaults.Keys.EXAMPLE_MESSAGE).isEqualTo("example_message")
    expectThat(RemoteConfigDefaults.Keys.EXAMPLE_NUMBER).isEqualTo("example_number")
  }
}
