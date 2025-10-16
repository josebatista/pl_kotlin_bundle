package io.github.josebatista.chirp.domain.exception

class SamePasswordException : RuntimeException("The new password can't be equal to the other one.")
