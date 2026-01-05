plugins {
  `kotlin-dsl`
}

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
  plugins {
    register("versionPlugin") {
      id = "caducity.version"
      implementationClass = "VersionPlugin"
    }
  }
}
