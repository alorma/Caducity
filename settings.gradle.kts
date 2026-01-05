enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
include(":feature:barcode_base")
include(":feature:barcode")
