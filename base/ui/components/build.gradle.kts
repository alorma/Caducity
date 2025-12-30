import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.jetbrains.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.jetbrains.compose.compiler)
  alias(libs.plugins.android.multiplatform.library)
}

kotlin {
  sourceSets.all {
    languageSettings.optIn("kotlin.time.ExperimentalTime")
    languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
    languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
  }

  androidLibrary {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }

    androidResources.enable = true

    namespace = "com.alorma.caducity.base.ui.components"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.base.ui.theme)
      implementation(projects.base.ui.icons)

      implementation(libs.compose.runtime)
      implementation(libs.compose.ui)
      implementation(libs.compose.foundation)
      implementation(libs.compose.ui.tooling.preview)

      implementation(libs.compose.material3)
      implementation(libs.alorma.settings.ui.base)
    }
    androidMain.dependencies {
      implementation(libs.compose.ui.tooling)

      implementation(libs.androidx.core)
    }
  }
}

