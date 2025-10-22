package io.github.josebatista.chirp.domain.exception

class UserAlreadyExistsException : RuntimeException("A user with this email or username already exists.")
