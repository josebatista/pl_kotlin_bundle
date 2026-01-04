package io.github.josebatista.chirp.infra.rate_limiting

import io.github.josebatista.chirp.infra.config.NginxConfig
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.stereotype.Component
import java.net.Inet4Address
import java.net.Inet6Address

@Component
class IpResolver(
    private val nginxConfig: NginxConfig
) {
    companion object {
        private val PRIVATE_IP_RANGES = listOf(
            "10.0.0.0/8",
            "172.16.0.0/12",
            "192.168.0.0/16",
            "127.0.0.0/8",
            "::1/128",
            "fc00::/7",
            "fe80::/10"
        ).map { IpAddressMatcher(it) }
        private val INVALID_IPS = listOf(
            "unknown",
            "unavailable",
            "0.0.0.0",
            "::"
        )
        private const val NGINX_REAL_IP_HEADER = "X-Real-IP"
        private const val FORWARDED_FOR_HEADER = "X-Forwarded-For"
    }

    private val logger = LoggerFactory.getLogger(IpResolver::class.java)

    private val trustedMatchers = nginxConfig
        .trustedIps
        .filter { it.isNotBlank() }
        .map { proxy ->
            val cidr = when {
                proxy.contains("/") -> proxy // Already has CIDR: "192.168.1.0/24"
                proxy.count { it == ':' } >= 2 -> "$proxy/128" // IPv6: "2001:db8::1" → "2001:db8::1/128"
                else -> "$proxy/32" // IPv4: "192.168.1.1" → "192.168.1.1/32"
            }
            IpAddressMatcher(cidr)
        }

    fun getClientIp(request: HttpServletRequest): String {
        val remoteAddr = request.remoteAddr
        if (!isFromTrustedIp(ip = remoteAddr)) {
            if (nginxConfig.requireProxy) {
                logger.warn("Direct attempt connection from $remoteAddr")
                throw SecurityException("No valid client IP in proxy headers")
            }
            return remoteAddr
        }
        val clientIp = extractFromXForwardedFor(request = request, proxyIp = remoteAddr)
            ?: extractFromXRealIp(request = request, proxyIp = remoteAddr)
        if (clientIp == null) {
            logger.warn("No valid client IP in proxy headers")
            if (nginxConfig.requireProxy) {
                throw SecurityException("No valid client IP in proxy headers")
            }
        }
        return clientIp ?: remoteAddr
    }

    private fun extractFromXForwardedFor(request: HttpServletRequest, proxyIp: String): String? {
        return request.getHeader(FORWARDED_FOR_HEADER)?.let { header ->
            header.split(",").firstOrNull()?.let { firstIp ->
                validateAndNormalizeIp(ip = firstIp.trim(), headerName = FORWARDED_FOR_HEADER, proxyIp = proxyIp)
            }
        }
    }

    private fun extractFromXRealIp(request: HttpServletRequest, proxyIp: String): String? {
        return request.getHeader(NGINX_REAL_IP_HEADER)?.let { header ->
            validateAndNormalizeIp(ip = header, headerName = NGINX_REAL_IP_HEADER, proxyIp = proxyIp)
        }
    }

    private fun validateAndNormalizeIp(ip: String, headerName: String, proxyIp: String): String? {
        val trimmedIp = ip.trim()
        if (trimmedIp.isBlank() || INVALID_IPS.contains(trimmedIp)) {
            logger.debug("Invalid IP in $headerName: $ip from proxy: $proxyIp")
            return null
        }
        return try {
            val inetAddr = when {
                trimmedIp.contains(":") -> Inet6Address.getByName(trimmedIp)
                trimmedIp.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")) -> Inet4Address.getByName(trimmedIp)
                else -> {
                    logger.warn("Invalid IP format in $headerName: $trimmedIp from proxy $proxyIp")
                    return null
                }
            }
            if (isPrivateIp(ip = inetAddr.hostAddress)) {
                logger.debug("Private IP in $headerName: $trimmedIp from proxy $proxyIp")
            }
            inetAddr.hostAddress
        } catch (e: Exception) {
            logger.warn("Invalid IP format in $headerName: $ip from proxy: $proxyIp", e)
            return null
        }
    }

    private fun isPrivateIp(ip: String): Boolean = PRIVATE_IP_RANGES.any { matcher -> matcher.matches(ip) }

    private fun isFromTrustedIp(ip: String): Boolean = trustedMatchers.any { matcher -> matcher.matches(ip) }
}
