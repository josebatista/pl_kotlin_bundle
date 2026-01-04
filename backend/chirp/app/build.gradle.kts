import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.chirp.spring.boot.app)
}

group = "io.github.josebatista"
version = "0.0.1-SNAPSHOT"
description = "Chirp Backend"

private fun AbstractCopyTask.copyResources(moduleName: String) {
    from(project(moduleName).projectDir.resolve("src/main/resources")) {
        into("")
    }
}

tasks {
    named<BootJar>("bootJar") {
        copyResources(":notification")
        copyResources(":user")
    }
}

dependencies {
    implementation(projects.chat)
    implementation(projects.common)
    implementation(projects.notification)
    implementation(projects.user)

    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.mail)
    runtimeOnly(libs.postgresql)
}
