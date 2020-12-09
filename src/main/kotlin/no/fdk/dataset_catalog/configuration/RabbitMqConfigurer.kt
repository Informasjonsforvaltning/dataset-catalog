package no.fdk.dataset_catalog.configuration

import org.springframework.amqp.core.*
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RabbitMQConfigurer (
    private val applicationProperties: ApplicationProperties
) {
    @Bean
    open fun converter(): Jackson2JsonMessageConverter = Jackson2JsonMessageConverter()

    @Bean
    open fun topicExchange(): TopicExchange =
        ExchangeBuilder
            .topicExchange(applicationProperties.exchangeName)
            .durable(false)
            .build()
}