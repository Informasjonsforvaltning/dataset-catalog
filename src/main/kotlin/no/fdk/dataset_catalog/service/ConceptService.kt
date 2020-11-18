package no.fdk.dataset_catalog.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.dataset_catalog.configuration.ApplicationProperties
import no.fdk.dataset_catalog.model.Concept
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

private val logger = LoggerFactory.getLogger(ConceptService::class.java)

@Service
class ConceptService(
    private val applicationProperties: ApplicationProperties
) {

    fun getConcepts(ids: List<String>): List<Concept> {
        return ids.mapNotNull { getConcept(it) }
    }

    private fun getConcept(id: String): Concept? {
        URL("${applicationProperties.conceptCatalogueHost}/api/concepts/$id")
            .openConnection()
            .run {
                this as HttpURLConnection
                if (responseCode != HttpStatus.OK.value()) {
                    logger.error("Error: $responseCode")
                    return null
                }
                val jsonBody = inputStream.bufferedReader().use(BufferedReader::readText)
                return try {
                    jacksonObjectMapper().readValue(jsonBody)
                } catch (t: Throwable) {
                    logger.error("Unable to parse response from concept catalogue for '$id'")
                    null
                }
            }
        }

}
