import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.jetbrains.kotlin.android)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.jetbrains.compose.compiler)
}

android {
  namespace = "com.alorma.caducity.base.ui.theme"

  compileSdk = libs.versions.android.compileSdk.get().toInt()
  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    // jvmTarget = "11"
  }
}

dependencies {
  implementation(libs.compose.runtime)
  implementation(libs.compose.ui)
  implementation(libs.compose.foundation)
  implementation(libs.compose.ui.tooling.preview)

  implementation(libs.compose.material3)

  implementation(libs.material.kolor)

  implementation(libs.alorma.settings.ui.base)

  implementation(libs.multiplatform.settings)
  implementation(libs.multiplatform.settings.no.arg)

  implementation(project.dependencies.platform(libs.koin.bom))
  implementation(libs.koin.compose)

  implementation(libs.compose.ui.tooling)

  implementation(libs.androidx.core)
}