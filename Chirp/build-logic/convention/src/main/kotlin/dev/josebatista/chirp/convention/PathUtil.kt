package dev.josebatista.chirp.convention

import org.gradle.api.Project
import java.util.Locale

internal fun Project.pathToPackageName(): String {
    val relativePackageName = path
        .replace(":", ".")
        .lowercase()
    return "dev.josebatista$relativePackageName"
}

internal fun Project.pathToResourcePrefix(): String {
    return path
        .replace(":", "_")
        .lowercase()
        .drop(1) + "_"
}

internal fun Project.pathToFrameworkName(): String = path
    .split(":", "-", "_", " ")
    .joinToString(separator = "") { part -> part.replaceFirstChar { it.titlecase(Locale.ROOT) } }
