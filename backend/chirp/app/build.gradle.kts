plugins {
    alias(libs.plugins.chirp.spring.boot.app)
}

group = "io.github.josebatista"
version = "0.0.1-SNAPSHOT"
description = "Chirp Backend"

dependencies {
    implementation(projects.chat)
    implementation(projects.common)
    implementation(projects.notification)
    implementation(projects.user)

    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.postgresql)
}
