plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.kotlin.jpa) apply false
}

group = "io.github.josebatista"
version = "0.0.1-SNAPSHOT"

subprojects {
    group = project.group
    version = project.version
}
