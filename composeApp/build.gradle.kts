import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
  alias(libs.plugins.jetbrains.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.kotlin.serialization)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.jetbrains.compose.compiler)
  alias(libs.plugins.jetbrains.compose.hotreload)
  // Temporarily commented out to allow building without Android SDK access
  // alias(libs.plugins.android.application)
}

kotlin {
  sourceSets.all {
    languageSettings.optIn("kotlin.time.ExperimentalTime")
    languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
    languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
  }

  // Temporarily commented out to allow building without Android SDK access
  /*
  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }
  */

  js {
    browser()
    binaries.executable()
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser {
      val rootDirPath = project.rootDir.path
      val projectDirPath = project.projectDir.path
      commonWebpackConfig {
        outputFileName = "composeApp.js"
        devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
          static = (static ?: mutableListOf()).apply {
            // Serve sources to debug inside browser
            add(rootDirPath)
            add(projectDirPath)
          }
        }
      }
    }
    binaries.executable()
  }

  jvm("desktop") {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(compose.runtime)
      implementation(compose.ui)
      implementation(compose.foundation)
      implementation(compose.components.resources)

      implementation(libs.compose.material3)

      implementation(libs.androidx.nav3.ui)
      implementation(libs.androidx.nav3.viewModel)
      implementation(libs.androidx.material3.adaptive)
      implementation(libs.androidx.material3.adaptive.nav3)

      implementation(libs.lifecycle.runtime.compose)

      implementation(libs.kotlinx.serialization.json)
      implementation(libs.kotlinx.datetime)
      implementation(libs.multiplatform.settings)
      implementation(libs.multiplatform.settings.no.arg)
      implementation(libs.alorma.settings.ui.tiles)
      implementation(libs.alorma.settings.ui.tiles.extended)

      // Koin
      implementation(project.dependencies.platform(libs.koin.bom))
      implementation(libs.koin.core)
      implementation(libs.koin.compose)
      implementation(libs.koin.compose.viewmodel)

      implementation(libs.kalendar)

      implementation(compose.components.uiToolingPreview)
    }
    /* Temporarily commented out to allow building without Android SDK access
    androidMain.dependencies {
      implementation(libs.androidx.activitycompose)
      implementation(libs.koin.android)

      implementation(compose.uiTooling)
    }
    */
    val desktopMain by getting {
      dependencies {
        implementation(compose.desktop.currentOs)
        implementation(libs.kotlinx.coroutines.swing)
      }
    }
  }
}

compose.desktop {
  application {
    mainClass = "com.alorma.caducity.MainKt"
    nativeDistributions {
      targetFormats(
        org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
        org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
        org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
      )
      packageName = "Caducity"
      packageVersion = "1.0.0"

      macOS {
        bundleID = "com.alorma.caducity"
      }
    }
  }
}

/* Temporarily commented out to allow building without Android SDK access
android {
  namespace = "com.alorma.caducity"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.alorma.caducity"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}
*/
