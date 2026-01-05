import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.Properties

class VersionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val extension = target.extensions.create("appVersion", VersionExtension::class.java, target)
    target.extensions.add("versionConfig", extension)

    // Automatically configure Android version when the Android plugin is applied
    target.plugins.withId("com.android.application") {
      val android = target.extensions.getByType(ApplicationExtension::class.java)
      android.defaultConfig {
        versionCode = extension.versionCode
        versionName = extension.versionName
      }
    }
  }
}

open class VersionExtension(private val project: Project) {
  private val versionProperties = Properties().apply {
    val versionFile = project.rootProject.file("version.properties")
    if (versionFile.exists()) {
      load(versionFile.inputStream())
    } else {
      throw IllegalStateException("version.properties file not found at ${versionFile.absolutePath}")
    }
  }

  val major: Int = versionProperties.getProperty("major").toInt()
  val minor: Int = versionProperties.getProperty("minor").toInt()
  val patch: Int = versionProperties.getProperty("patch").toInt()
  val snapshot: Boolean = versionProperties.getProperty("snapshot").toBoolean()

  val versionName: String = buildString {
    append("$major.$minor.$patch")
    if (snapshot) {
      append("-snapshot")
    }
  }

  val versionCode: Int = run {
    val majorPart = major.toString().padStart(3, '0')
    val minorPart = minor.toString().padStart(3, '0')
    val patchPart = patch.toString().padStart(3, '0')
    val snapshotPart = if (snapshot) "0" else "1"

    "$majorPart$minorPart$patchPart$snapshotPart".toInt()
  }
}
