package no.fdk.dataset_catalog.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.dataset_catalog.configuration.ApplicationProperties
import no.fdk.dataset_catalog.model.Concept
import no.fdk.dataset_catalog.model.ElasticConceptsResult
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
        URL("${applicationProperties.searchFulltextHost}/concepts")
            .openConnection()
            .run {
                this as HttpURLConnection
                this.setRequestProperty("Accept", "application/json")
                this.setRequestProperty("Content-Type", "application/json")
                this.requestMethod = "POST"
                this.doOutput = true
                this.getOutputStream().write("""{"filters": [{"_id": "$id"}]}""".toByteArray())

                if (responseCode != HttpStatus.OK.value()) {
                    logger.error("Error: $responseCode")
                    return null
                }

                return try {
                    return jacksonObjectMapper()
                        .readValue(inputStream, ElasticConceptsResult::class.java)
                        ?.hits
                        ?.firstOrNull()
                } catch (t: Throwable) {
                    logger.error("Unable to parse response from concept catalogue for '$id'", t)
                    null
                }
            }
        }

}
