package no.fdk.dataset_catalog.service

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import no.fdk.dataset_catalog.configuration.ApplicationProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.schedule

private val logger = LoggerFactory.getLogger(PublishingService::class.java)

@Service
class PublishingService(
    private val rabbitTemplate: RabbitTemplate,
    private val applicationProperties: ApplicationProperties,
) {

    private val toHarvest = mutableMapOf<String, TimerTask>()

    @Synchronized
    fun triggerHarvest(catalogId: String) {
        logger.info("Scheduling harvest for ${LocalDateTime.now().plusSeconds(applicationProperties.harvestDelay/1000)} on catalog $catalogId")

        if (toHarvest.containsKey(catalogId)) {
            toHarvest[catalogId]?.cancel()
        }

        toHarvest[catalogId] = Timer(catalogId, false).schedule(applicationProperties.harvestDelay) {
            sendHarvestMessage(catalogId)
        }
    }

    private fun sendHarvestMessage(catalogId: String) {
        logger.info("Sending harvest message to queue for catalog with ID $catalogId")
        val payload = JsonNodeFactory.instance.objectNode()
        payload.put("publisherId", catalogId)
        try {
            rabbitTemplate.convertAndSend(applicationProperties.catalogHarvestRoute, payload)
            logger.info("Successfully sent harvest message for publisher $catalogId")
            toHarvest.remove(catalogId)
        } catch (e: Exception) {
            logger.error("Failed to send harvest message for publisher $catalogId", e)
        }
    }

    fun sendNewDataSourceMessage(publisherId: String?, url: String?): Boolean {
        logger.info("Adding data source for $publisherId")

        if (!publisherId.isNullOrEmpty() && !url.isNullOrEmpty()) {
            val payload = JsonNodeFactory.instance.objectNode()
            payload.put("publisherId", publisherId)
            payload.put("url", url)
            payload.put("dataSourceType", "DCAT-AP-NO")
            payload.put("dataType", "dataset")
            payload.put("acceptHeaderValue", "text/turtle")
            payload.put("description", "Automatically generated data source for $publisherId")

            try {
                rabbitTemplate.convertAndSend(applicationProperties.newDataSourceRoute, payload)
                logger.info("Successfully sent data source message for $publisherId")
                return true
            } catch (e: Exception) {
                logger.error("Failed to send data source message for $publisherId", e)
            }
        } else {
            logger.warn("New data source message could not be sent for catalog with id $publisherId")
        }
        return false
    }
}