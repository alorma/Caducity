import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.jetbrains.kotlin.android)
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
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_11
    optIn.add("kotlin.RequiresOptIn")
    optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
    optIn.add("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
  }
}

dependencies {
  implementation(projects.base.main)

  // Compose BOM
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.ui.tooling.preview)
  debugImplementation(libs.androidx.compose.ui.tooling)

  implementation(libs.androidx.compose.material3)

  implementation(libs.material.kolor)

  implementation(libs.alorma.settings.ui.base)

  implementation(libs.multiplatform.settings)
  implementation(libs.multiplatform.settings.no.arg)

  implementation(project.dependencies.platform(libs.koin.bom))
  implementation(libs.koin.compose)

  implementation(libs.androidx.core)
}