import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.jetbrains.kotlin.multiplatform)
  alias(libs.plugins.android.multiplatform.library)
}

kotlin {
  sourceSets.all {
    languageSettings.optIn("kotlin.time.ExperimentalTime")
  }

  androidLibrary {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }

    androidResources.enable = false

    namespace = "com.alorma.caducity.base.main"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.kotlinx.datetime)
    }
    androidMain.dependencies {
      // Add Android-specific dependencies here when needed
    }
  }
}
