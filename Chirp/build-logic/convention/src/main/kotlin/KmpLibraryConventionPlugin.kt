import com.android.build.api.dsl.LibraryExtension
import dev.josebatista.chirp.convention.configureKotlinAndroid
import dev.josebatista.chirp.convention.configureKotlinMultiplatform
import dev.josebatista.chirp.convention.libs
import dev.josebatista.chirp.convention.pathToResourcePrefix
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class KmpLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.library")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }
            configureKotlinMultiplatform()
            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                resourcePrefix = pathToResourcePrefix()
                // Required to make debug build of app run in iOS simulator
                experimentalProperties["android.experimental.kmp.enableAndroidResources"] = "true"
            }
            dependencies {
                "commonMainImplementation"(libs.findLibrary("kotlinx-serialization-json").get())
                "commonTestImplementation"(libs.findLibrary("kotlin-test").get())
            }
        }
    }
}
