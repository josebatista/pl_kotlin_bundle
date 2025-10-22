package io.github.josebatista.chirp.api.config

import io.github.josebatista.chirp.domain.exception.RateLimitException
import io.github.josebatista.chirp.infra.rate_limiting.IpRateLimiter
import io.github.josebatista.chirp.infra.rate_limiting.IpResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration

@Component
class IpRateLimitInterceptor(
    private val ipRateLimiter: IpRateLimiter,
    private val ipResolver: IpResolver,
    @param:Value($$"${chirp.rate-limit.ip.apply-limit}")
    private val applyLimit: Boolean,
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (handler is HandlerMethod && applyLimit) {
            val annotation = handler.getMethodAnnotation(IpRateLimit::class.java)
            if (annotation != null) {
                return try {
                    val clientIp = ipResolver.getClientIp(request = request)
                    val path = request.requestURI
                    ipRateLimiter.withIpRateLimit(
                        ipAddress = clientIp,
                        path = path,
                        resetsIn = Duration.of(
                            annotation.duration,
                            annotation.unit.toChronoUnit()
                        ),
                        maxRequestsPerIp = annotation.requests,
                        action = { true }
                    )
                } catch (_: RateLimitException) {
                    response.sendError(HttpStatus.TOO_MANY_REQUESTS.value())
                    false
                }
            }
        }
        return true
    }
}
