import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)

  alias(libs.plugins.jetbrains.compose.compiler)

  alias(libs.plugins.google.ksp)
  alias(libs.plugins.jetbrains.kotlin.serialization)

  alias(libs.plugins.jetbrains.kotlin.parcelize)

  alias(libs.plugins.compose.screenshot)

  alias(libs.plugins.google.services)

  id("caducity.version")
}

android {
  namespace = "com.alorma.caducity"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.alorma.caducity"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()

    // Read debug App Check token from environment or local.properties
    val localProperties = file("../local.properties")
    val debugToken = if (localProperties.exists()) {
      val properties = Properties()
      properties.load(localProperties.inputStream())
      properties.getProperty("DEBUG_APP_CHECK_TOKEN") ?: System.getenv("DEBUG_APP_CHECK_TOKEN")
      ?: ""
    } else {
      System.getenv("DEBUG_APP_CHECK_TOKEN") ?: ""
    }

    buildConfigField("String", "DEBUG_APP_CHECK_TOKEN", "\"$debugToken\"")
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
  signingConfigs {
    register("release") {
      val realKeystoreFile = file("../certs/release.keystore")
      val fakeKeystoreFile = file("../certs/fakeRelease.keystore")

      if (realKeystoreFile.exists() && System.getenv("KEYSTORE_PASSWORD") != null) {
        storeFile = realKeystoreFile
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "no_value"
        keyAlias = System.getenv("KEY_ALIAS") ?: "no_value"
        keyPassword = System.getenv("KEY_PASSWORD") ?: "no_value"
      } else {
        storeFile = fakeKeystoreFile
        storePassword = "caducity"
        keyAlias = "caducity"
        keyPassword = "caducity"
      }
    }
    named("debug") {
      storeFile = file("../certs/debug.keystore")
    }
  }
  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true

      signingConfig = signingConfigs["release"]
    }
    debug {
      applicationIdSuffix = ".dev"
      signingConfig = signingConfigs["debug"]
    }
  }
  buildFeatures {
    buildConfig = true
    compose = true
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }

  experimentalProperties["android.experimental.enableScreenshotTest"] = true

  screenshotTests {
    imageDifferenceThreshold = 0.01f
  }
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_21
    optIn.add("kotlin.time.ExperimentalTime")
    optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
    optIn.add("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
  }
}

ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
  implementation(projects.icons)

  // Compose BOM
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui.tooling.preview)
  debugImplementation(libs.androidx.compose.ui.tooling)

  implementation(libs.material.kolor)

  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.lifecycle.viewmodel)

  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.collections.immutable)
  implementation(libs.multiplatform.settings)
  implementation(libs.multiplatform.settings.no.arg)
  implementation(libs.fireandforget.core)
  implementation(libs.fireandforget.multiplatform.settings)
  implementation(libs.alorma.settings.ui.tiles)
  implementation(libs.alorma.settings.ui.tiles.extended)
  implementation(libs.alorma.settings.ui.tiles.expressive)

  // Koin
  implementation(project.dependencies.platform(libs.koin.bom))
  implementation(libs.koin.compose)
  implementation(libs.koin.compose.viewmodel)

  implementation(libs.kalendar)

  implementation(libs.androidx.activitycompose)
  implementation(libs.androidx.appcompat)

  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  ksp(libs.androidx.room.compiler)

  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.koin.androidx.workmanager)

  // Timber (logging)
  implementation(libs.timber)

  // Firebase
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.ai)
  implementation(libs.firebase.appcheck)
  implementation(libs.firebase.appcheck.playintegrity)
  implementation(libs.firebase.appcheck.debug)

  // Testing
  testImplementation(libs.junit)
  testImplementation(libs.strikt.core)

  // Screenshot testing
  screenshotTestImplementation(libs.screenshot.validation.api)

  screenshotTestImplementation(platform(libs.androidx.compose.bom))
  screenshotTestImplementation(libs.androidx.compose.ui.test.junit4)
  screenshotTestImplementation(libs.androidx.compose.ui.test.manifest)
  screenshotTestImplementation(libs.androidx.compose.ui.tooling)
  screenshotTestImplementation(libs.androidx.compose.ui)

  implementation(projects.feature.barcodeBase)
}
