plugins {
    id("java-library")
    alias(libs.plugins.chirp.spring.boot.service)
    alias(libs.plugins.kotlin.jpa)
}

group = "io.github.josebatista"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(projects.common)

    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.postgresql)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
