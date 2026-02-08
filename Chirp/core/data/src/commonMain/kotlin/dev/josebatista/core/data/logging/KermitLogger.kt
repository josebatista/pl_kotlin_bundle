package dev.josebatista.core.data.logging

import co.touchlab.kermit.Logger
import dev.josebatista.core.domain.logging.ChirpLogger

object KermitLogger : ChirpLogger {
    override fun debug(message: String) {
        Logger.d(messageString = message)
    }

    override fun info(message: String) {
        Logger.i(messageString = message)
    }

    override fun warn(message: String) {
        Logger.w(messageString = message)
    }

    override fun error(message: String, throwable: Throwable?) {
        Logger.e(messageString = message, throwable = throwable)
    }
}
