package no.fdk.dataset_catalog.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.fdk.dataset_catalog.configuration.ApplicationProperties
import no.fdk.dataset_catalog.model.Organization
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

private val logger = LoggerFactory.getLogger(OrganizationService::class.java)

@Service
class OrganizationService(
    private val applicationProperties: ApplicationProperties) {

    fun hasDelegationPermission(id: String): Boolean {
        val allowDelegatedRegistration: Boolean? = getOrganization(id)?.allowDelegatedRegistration
        return allowDelegatedRegistration != null && allowDelegatedRegistration
    }

    fun getOrganization(organizationNumber: String?): Organization? {
        URL("${applicationProperties.organizationCatalogueHost}/organizations/$organizationNumber")
            .openConnection()
            .run {
                this as HttpURLConnection
                this.setRequestProperty("Accept", "application/json")
                if (responseCode != HttpStatus.OK.value()) {
                    logger.error("Error: $responseCode", Exception("Error: $responseCode"))
                    return null
                }
                val jsonBody = inputStream.bufferedReader().use(BufferedReader::readText)
                return try {
                    jacksonObjectMapper().readValue(jsonBody)
                } catch (t: Throwable) {
                    logger.error("Unable to parse response from organization catalogue for '$organizationNumber'", t)
                    null
                }
            }
    }
}