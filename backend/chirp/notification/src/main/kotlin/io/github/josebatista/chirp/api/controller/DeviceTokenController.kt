package io.github.josebatista.chirp.api.controller

import io.github.josebatista.chirp.api.dto.DeviceTokenDto
import io.github.josebatista.chirp.api.dto.RegisterDeviceRequest
import io.github.josebatista.chirp.api.mappers.toDeviceTokenDto
import io.github.josebatista.chirp.api.mappers.toPlatform
import io.github.josebatista.chirp.api.util.requestUserId
import io.github.josebatista.chirp.service.PushNotificationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/notification")
class DeviceTokenController(
    private val pushNotificationService: PushNotificationService
) {

    @PostMapping("/register")
    fun registerDeviceToken(
        @Valid @RequestBody body: RegisterDeviceRequest
    ): DeviceTokenDto {
        return pushNotificationService.registerDevice(
            userId = requestUserId,
            token = body.token,
            platform = body.platform.toPlatform()
        ).toDeviceTokenDto()
    }

    @DeleteMapping("/{token}")
    fun unregisterDeviceToken(
        @PathVariable("token") token: String
    ) {
        pushNotificationService.unregisterDevice(token = token)
    }
}
