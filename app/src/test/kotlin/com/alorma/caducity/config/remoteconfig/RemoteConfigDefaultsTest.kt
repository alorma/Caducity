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
      containsKey("example_feature_enabled")
      containsKey("example_message")
      containsKey("example_number")
    }
  }

  @Test
  fun `defaults map has expected size`() {
    // If you add new keys, update this test
    expectThat(RemoteConfigDefaults.defaults).hasSize(3)
  }

  @Test
  fun `example feature enabled has boolean default value`() {
    val value = RemoteConfigDefaults.defaults["example_feature_enabled"]
    expectThat(value).isEqualTo(false)
  }

  @Test
  fun `example message has non-empty string default value`() {
    val value = RemoteConfigDefaults.defaults["example_message"] as String
    expectThat(value).isNotEmpty()
  }

  @Test
  fun `example number has positive long default value`() {
    val value = RemoteConfigDefaults.defaults["example_number"] as Long
    expectThat(value).isEqualTo(42L)
  }

  @Test
  fun `keys are correctly defined`() {
    // Verify key constants are defined
    expectThat(RemoteConfigDefaults.defaults.keys).isNotEmpty()
  }
}
