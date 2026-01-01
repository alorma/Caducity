import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.jetbrains.kotlin.android)

  alias(libs.plugins.jetbrains.compose.compiler)

  alias(libs.plugins.google.ksp)
  alias(libs.plugins.jetbrains.kotlin.serialization)
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
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
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

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_11
    optIn.add("kotlin.time.ExperimentalTime")
    optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
    optIn.add("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
  }
}

ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
  implementation(projects.base.main)
  implementation(projects.base.ui.theme)
  implementation(projects.base.ui.icons)
  implementation(projects.base.ui.components)

  // Compose BOM
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui.tooling.preview)
  debugImplementation(libs.androidx.compose.ui.tooling)

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

  implementation(libs.scan.engine)
}
