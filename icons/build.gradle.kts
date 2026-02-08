import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.jetbrains.compose.compiler)
}

android {
  namespace = "com.alorma.caducity.base.ui.icons"

  compileSdk =
    libs.versions.android.compileSdk
      .get()
      .toInt()
  defaultConfig {
    minSdk =
      libs.versions.android.minSdk
        .get()
        .toInt()
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_21
  }
}

dependencies {
  // Compose BOM
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.foundation)
}
