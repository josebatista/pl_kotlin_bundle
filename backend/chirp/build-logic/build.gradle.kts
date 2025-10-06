plugins {
    `kotlin-dsl`
}

dependencies {
//    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
    implementation(libs.kotlin.gradle.plugin)
//    implementation("org.jetbrains.kotlin:kotlin-allopen:2.2.0")
    implementation(libs.kotlin.allopen)
//    implementation("org.springframework.boot:spring-boot-gradle-plugin:4.0.0-SNAPSHOT")
    implementation(libs.spring.boot.gradle.plugin)
//    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
    implementation(libs.spring.boot.dependency.management)
}