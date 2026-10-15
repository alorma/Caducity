import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.test)
  alias(libs.plugins.androidx.baselineprofile)
}

android {
  namespace = "com.alorma.caducity.baselineprofile"

  compileSdk =
    libs.versions.android.compileSdk
      .get()
      .toInt()
  defaultConfig {
    minSdk =
      libs.versions.android.minSdk
        .get()
        .toInt()
    targetSdk =
      libs.versions.android.targetSdk
        .get()
        .toInt()
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }

  targetProjectPath = ":app"
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_21
  }
}

// El perfil es genera al dispositiu/emulador connectat (adb); no hi ha dispositius gestionats
baselineProfile {
  useConnectedDevices = true
}

dependencies {
  implementation(libs.androidx.test.ext.junit)
  implementation(libs.androidx.test.uiautomator)
  implementation(libs.androidx.benchmark.macro.junit4)
}
