plugins {
    id("java-library")
    alias(libs.plugins.chirp.kotlin.common)
    alias(libs.plugins.spring.boot)
}

group = "io.github.josebatista"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}