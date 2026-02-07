plugins {
  // this is necessary to avoid the plugins to be loaded multiple times
  // in each subproject's classloader
  alias(libs.plugins.android.application) apply false

  alias(libs.plugins.jetbrains.compose.compiler) apply false

  alias(libs.plugins.jetbrains.kotlin.serialization) apply false
  alias(libs.plugins.jetbrains.kotlin.parcelize) apply false

  alias(libs.plugins.android.library) apply false

  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.firebase.crashlytics) apply false

  alias(libs.plugins.ktlint) apply false
}

subprojects {
  apply(plugin = "org.jlleitschuh.gradle.ktlint")

  configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("1.5.0")
    android.set(true)
    ignoreFailures.set(false)
    reporters {
      reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
      reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
    filter {
      exclude("**/generated/**")
      exclude("**/build/**")
    }
  }
}
