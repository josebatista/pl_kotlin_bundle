package dev.josebatista.chirp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform