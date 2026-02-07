enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Caducity"

pluginManagement {
  includeBuild("build-logic")
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
  }
}

dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
  }
}

include(":app")
include(":icons")
