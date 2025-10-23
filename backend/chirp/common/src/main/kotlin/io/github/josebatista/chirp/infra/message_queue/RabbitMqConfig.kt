package io.github.josebatista.chirp.infra.message_queue

import io.github.josebatista.chirp.domain.events.ChirpEvent
import io.github.josebatista.chirp.domain.events.user.UserEventConstants
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import tools.jackson.databind.DefaultTyping
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import tools.jackson.databind.jsontype.PolymorphicTypeValidator
import tools.jackson.module.kotlin.kotlinModule

@Configuration
@EnableTransactionManagement
class RabbitMqConfig {

    @Bean
    fun messageConverter(): JacksonJsonMessageConverter {
        val polymorphicTypeValidator: PolymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(/* baseOfBase = */ ChirpEvent::class.java)
            .allowIfSubType(/* prefixForSubType = */ "java.util.") // Allow Java Lists
            .allowIfSubType(/* prefixForSubType = */ "kotlin.collections.") // Kotlin Collections
            .build()
        val objectMapper = JsonMapper.builder()
            .addModule(/* module = */ kotlinModule())
            .polymorphicTypeValidator(/* ptv = */ polymorphicTypeValidator)
            .activateDefaultTyping(
                /* subtypeValidator = */ polymorphicTypeValidator,
                /* dti = */ DefaultTyping.NON_FINAL
            )
            .build()
        return JacksonJsonMessageConverter(/* jsonMapper = */ objectMapper).apply {
            typePrecedence = JacksonJavaTypeMapper.TypePrecedence.TYPE_ID
        }
    }

    @Bean
    fun rabbitListenerContainerFactory(
        connectionFactory: ConnectionFactory,
        transactionManager: PlatformTransactionManager,
        messageConverter: JacksonJsonMessageConverter
    ): SimpleRabbitListenerContainerFactory {
        return SimpleRabbitListenerContainerFactory().apply {
            this.setConnectionFactory(connectionFactory)
            this.setTransactionManager(transactionManager)
            this.setChannelTransacted(true)
            this.setMessageConverter(messageConverter)
        }
    }

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: JacksonJsonMessageConverter
    ): RabbitTemplate {
        return RabbitTemplate(/* connectionFactory = */ connectionFactory).apply {
            this.messageConverter = messageConverter
        }
    }

    @Bean
    fun userExchange() = TopicExchange(
        /* name = */ UserEventConstants.USER_EXCHANGE,
        /* durable = */ true,
        /* autoDelete = */ false
    )

    @Bean
    fun notificationUserEventsQueue() = Queue(
        /* name = */ MessageQueue.NOTIFICATION_USER_EVENTS,
        /* durable = */ true
    )

    @Bean
    fun notificationUserEventsBinding(
        notificationUserEventsQueue: Queue,
        userExchange: TopicExchange
    ): Binding {
        return BindingBuilder
            .bind(/* queue = */ notificationUserEventsQueue)
            .to(/* exchange = */ userExchange)
            .with(/* routingKey = */ "user.*")
    }
}
