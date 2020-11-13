package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.model.Organization
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

private val logger = LoggerFactory.getLogger(OrganizationService::class.java)

@Service
class OrganizationService {
    private val restTemplate = RestTemplate()
    private val defaultHeaders = HttpHeaders()
    @Value("\${application.openDataEnhetProxy}")
    private val organizationCatalogueUrl: String? = null

    init {
        defaultHeaders.accept = listOf(MediaType.APPLICATION_JSON)
    }

    fun getByOrgNr(orgnr: String): Organization? {
        val url = organizationCatalogueUrl + orgnr
        defaultHeaders.accept = listOf(MediaType.APPLICATION_JSON)
        val entity: HttpEntity<String> = HttpEntity("body", defaultHeaders)
        return try {
            restTemplate.exchange(url, HttpMethod.GET, entity, Organization::class.java).body
        } catch (e: Exception) {
            logger.error("Getting organization by ID $orgnr failed.", e)
            return null
        }
    }

    fun hasDelegationPermission(id: String): Boolean {
        val allowDelegatedRegistration: Boolean? = getOrganization(id)?.allowDelegatedRegistration
        return allowDelegatedRegistration != null && allowDelegatedRegistration
    }

    private fun getOrganization(organizationNumber: String?): Organization? {
        val restTemplate = RestTemplate()

        return try {
            restTemplate.exchange(String.format("%s/organizations/%s", organizationCatalogueUrl, organizationNumber),
                HttpMethod.GET,
                HttpEntity<Any?>(defaultHeaders),
                Organization::class.java)
                .body

        } catch (exception: HttpClientErrorException) {
            if (exception.statusCode == HttpStatus.NOT_FOUND) {
                throw Exception(String.format("Organization with ID %s not found", organizationNumber), exception)
            }
            throw Exception("An unexpected client error occurred", exception)
        } catch (exception: ResourceAccessException) {
            throw Exception("Downstream service not available", exception)
        } catch (exception: RestClientException) {
            throw Exception("An unexpected server error occurred", exception)
        }
    }
}