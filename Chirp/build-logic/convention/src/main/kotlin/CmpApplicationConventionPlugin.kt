import dev.josebatista.chirp.convention.configureAndroidTarget
import dev.josebatista.chirp.convention.configureIosTargets
import dev.josebatista.chirp.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class CmpApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("dev.josebatista.convention.android.application.compose")
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.jetbrains.compose")
            }
            configureAndroidTarget()
            configureIosTargets()
            dependencies {
                "debugImplementation"(libs.findLibrary("ui-tooling").get())
            }
        }
    }
}
