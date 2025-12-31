import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.jetbrains.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.kotlin.serialization)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.jetbrains.compose.compiler)
  alias(libs.plugins.android.application)
  alias(libs.plugins.google.ksp)
  alias(libs.plugins.androidx.room)
}

kotlin {
  sourceSets.all {
    languageSettings.optIn("kotlin.time.ExperimentalTime")
    languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
    languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
  }

  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.base.main)
      implementation(projects.base.ui.theme)
      implementation(projects.base.ui.icons)
      implementation(projects.base.ui.components)

      implementation(libs.compose.runtime)
      implementation(libs.compose.ui)
      implementation(libs.compose.foundation)
      implementation(libs.compose.components.resources)

      implementation(libs.compose.material3)

      implementation(libs.androidx.nav3.ui)
      implementation(libs.androidx.nav3.viewModel)
      implementation(libs.androidx.material3.adaptive)
      implementation(libs.androidx.material3.adaptive.nav3)

      implementation(libs.lifecycle.runtime.compose)

      implementation(libs.kotlinx.serialization.json)
      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.collections.immutable)
      implementation(libs.multiplatform.settings)
      implementation(libs.multiplatform.settings.no.arg)
      implementation(libs.alorma.settings.ui.tiles)
      implementation(libs.alorma.settings.ui.tiles.extended)
      implementation(libs.alorma.settings.ui.tiles.expressive)

      // Koin
      implementation(project.dependencies.platform(libs.koin.bom))
      implementation(libs.koin.compose)
      implementation(libs.koin.compose.viewmodel)

      implementation(libs.kalendar)

      implementation(libs.compose.ui.tooling.preview)
    }
    androidMain.dependencies {
      implementation(libs.compose.ui.tooling)

      implementation(libs.androidx.activitycompose)
      implementation(libs.androidx.appcompat)

      implementation(libs.androidx.room.runtime)
      implementation(libs.androidx.sqlite.bundled)
      implementation(libs.androidx.room.sqlite.wrapper)

      implementation(libs.androidx.work.runtime.ktx)
      implementation(libs.koin.androidx.workmanager)

      implementation(libs.scan.engine)
    }
  }
}

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
  buildFeatures {
    buildConfig = true
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

// Apply parcelize plugin after android plugin configuration
plugins.apply("org.jetbrains.kotlin.plugin.parcelize")

room {
  schemaDirectory("$projectDir/schemas")
}

dependencies {
  add("kspAndroid", libs.androidx.room.compiler)
}
