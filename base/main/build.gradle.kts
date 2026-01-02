import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.jetbrains.kotlin.android)
}

android {
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  namespace = "com.alorma.caducity.base.main"

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
    optIn.add("kotlin.time.ExperimentalTime")
  }
}

dependencies {
  implementation(libs.kotlinx.datetime)

  implementation(project.dependencies.platform(libs.koin.bom))
  implementation(libs.koin.core)
}
