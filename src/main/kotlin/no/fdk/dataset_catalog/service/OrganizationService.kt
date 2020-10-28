package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.model.Organization
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class OrganizationService() {
    @Value("\${application.openDataEnhetProxy}")
    private val openDataEnhetsregisteretProxy: String? = null

    fun getByOrgNr(orgnr: String): Organization? {
        val url = openDataEnhetsregisteretProxy + orgnr
        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        val entity: HttpEntity<String> = HttpEntity("body", headers)
        return restTemplate.exchange(url, HttpMethod.GET, entity, Organization::class.java).body
    }
}