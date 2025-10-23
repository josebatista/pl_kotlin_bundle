plugins {
    id("java-library")
    alias(libs.plugins.chirp.kotlin.common)
}

group = "io.github.josebatista"
version = "0.0.1-SNAPSHOT"

dependencies {
    api(libs.jackson.module.kotlin)
    api(libs.kotlin.reflect)

    implementation(libs.spring.boot.starter.amqp)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
