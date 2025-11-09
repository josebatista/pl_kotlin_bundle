package io.github.josebatista.chirp.infra.caching

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import tools.jackson.databind.DefaultTyping
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import tools.jackson.databind.jsontype.PolymorphicTypeValidator
import tools.jackson.module.kotlin.kotlinModule
import java.time.Duration

@Configuration
@EnableCaching
class RedisConfig {

    @Bean
    fun cacheManager(
        connectionFactory: LettuceConnectionFactory,
    ): RedisCacheManager {
        val polymorphicTypeValidator: PolymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType(/* prefixForSubType = */ "java.util.") // Allow Java Lists
            .allowIfSubType(/* prefixForSubType = */ "kotlin.collections.") // Kotlin Collections
            .allowIfSubType(/* prefixForSubType = */ "io.github.josebatista.chirp.")
            .build()
        val objectMapper = JsonMapper.builder()
//            .addModule(JavaTimeModule())
            .addModule(/* module = */ kotlinModule())
            .polymorphicTypeValidator(/* ptv = */ polymorphicTypeValidator)
            .activateDefaultTyping(
                /* subtypeValidator = */ polymorphicTypeValidator,
                /* dti = */ DefaultTyping.NON_FINAL
            )
            .build()
        val cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(/* hours = */ 1L))
            .serializeValuesWith(
                /* valueSerializationPair = */ RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJacksonJsonRedisSerializer(/* mapper = */ objectMapper)
                )
            )
        return RedisCacheManager
            .builder(/* connectionFactory = */ connectionFactory)
            .cacheDefaults(/* defaultCacheConfiguration = */ cacheConfig)
            .withCacheConfiguration(
                /* cacheName = */ "messages",
                /* cacheConfiguration = */ cacheConfig.entryTtl(Duration.ofMinutes(/* minutes = */ 30L))
            )
            .build()
    }
}
