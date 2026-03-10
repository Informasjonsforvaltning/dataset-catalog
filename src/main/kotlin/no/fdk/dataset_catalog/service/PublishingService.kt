package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.configuration.ApplicationProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import java.net.URI

private val logger = LoggerFactory.getLogger(PublishingService::class.java)

@Service
class PublishingService(
    private val applicationProperties: ApplicationProperties,
    private val restTemplate: RestTemplate = RestTemplate(),
) {

    fun createNewDataSource(catalogId: String) {
        val baseUri = applicationProperties.harvestAdminUri

        val url = "$baseUri/organizations/$catalogId/datasources"

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            resolveBearerToken()?.let { set(HttpHeaders.AUTHORIZATION, "Bearer $it") }
        }

        val body = HarvestAdminDataSource(
            dataSourceType = "DCAT-AP-NO",
            dataType = "dataset",
            url = "${applicationProperties.datasetCatalogUriHost}/$catalogId",
            acceptHeaderValue = "text/turtle",
            publisherId = catalogId,
            description = "Automatically generated data source for $catalogId"
        )

        runCatching {
            restTemplate.postForEntity<Any>(URI(url), HttpEntity(body, headers))
        }.onFailure {
            logger.error("Error calling Harvest Admin createDataSource for catalog {}", catalogId, it)
        }
    }

    fun triggerHarvest(catalogId: String) {
        val baseUri = applicationProperties.harvestAdminUri

        val url = "$baseUri/organizations/$catalogId/datasources/start-harvesting"

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            resolveBearerToken()?.let { set(HttpHeaders.AUTHORIZATION, "Bearer $it") }
        }

        val body = StartHarvestByUrlRequest(
            url = "${applicationProperties.datasetCatalogUriHost}/$catalogId",
            dataType = "dataset",
        )

        runCatching {
            restTemplate.postForEntity<Any>(URI(url), HttpEntity(body, headers))
        }.onFailure {
            logger.error("Error calling Harvest Admin startHarvestingByUrlAndDataType for catalog {}", catalogId, it)
        }
    }

    private fun resolveBearerToken(): String? =
        (SecurityContextHolder.getContext().authentication?.principal as? Jwt)
            ?.tokenValue
}

private data class HarvestAdminDataSource(
    val dataSourceType: String,
    val dataType: String,
    val url: String,
    val acceptHeaderValue: String? = null,
    val publisherId: String,
    val description: String? = null,
)

data class StartHarvestByUrlRequest(
    val url: String,
    val dataType: String,
)
