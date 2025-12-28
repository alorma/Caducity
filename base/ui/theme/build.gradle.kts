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

    namespace = "com.alorma.caducity.base.ui.theme"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.compose.runtime)
      implementation(libs.compose.ui)
      implementation(libs.compose.foundation)
      implementation(libs.compose.ui.tooling.preview)

      implementation(libs.compose.material3)

      implementation(libs.multiplatform.settings)
      implementation(libs.multiplatform.settings.no.arg)

      implementation(project.dependencies.platform(libs.koin.bom))
      implementation(libs.koin.compose)
    }
    androidMain.dependencies {
      implementation(libs.compose.ui.tooling)

      implementation(libs.androidx.core)
    }
  }
}

