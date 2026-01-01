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
  kotlinOptions {
    // jvmTarget = "11"
  }
}

dependencies {
  implementation(libs.kotlinx.datetime)
}
