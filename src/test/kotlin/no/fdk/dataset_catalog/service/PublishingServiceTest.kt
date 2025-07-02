package no.fdk.dataset_catalog.service

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import no.fdk.dataset_catalog.configuration.ApplicationProperties
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.amqp.rabbit.core.RabbitTemplate

@Tag("unit")
class PublishingServiceTest {
    private val rabbitTemplate: RabbitTemplate = mock()
    private val applicationProperties = ApplicationProperties(
        "localhost:5050",
        "localhost:5050",
        "localhost:5050",
        "localhost:5050",
        50L,
    "catalogHarvestRoute",
    "newDataSourceRoute",
    "harvests")
    private val publishingService = PublishingService(rabbitTemplate, applicationProperties)

    @Nested
    internal inner class TriggerHarvest {
        @Test
        fun`triggers harvest only once`() {
            val catalogId = "cat1"

            val payload = JsonNodeFactory.instance.objectNode()
            payload.put("publisherId", catalogId)

            for (i in 1..100) {
                publishingService.triggerHarvest(catalogId)
            }

            Thread.sleep(applicationProperties.harvestDelay*2)

            verify(rabbitTemplate, times(1)).convertAndSend(applicationProperties.catalogHarvestRoute, payload)
        }

        @Test
        fun`triggers harvest after delay`() {
            val catalogId = "cat1"

            val payload = JsonNodeFactory.instance.objectNode()
            payload.put("publisherId", catalogId)

            for (i in 1..3) {
                publishingService.triggerHarvest(catalogId)
                Thread.sleep(applicationProperties.harvestDelay*2)

            }

            verify(rabbitTemplate, times(3)).convertAndSend(applicationProperties.catalogHarvestRoute, payload)
        }
    }
}