plugins {
    id("java-library")
    alias(libs.plugins.chirp.kotlin.common)
}

group = "io.github.josebatista"
version = "0.0.1-SNAPSHOT"

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}