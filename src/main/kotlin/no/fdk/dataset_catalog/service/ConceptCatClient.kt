package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.model.Concept
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ConceptCatClientService(
    private val environment: Environment,
    private val apiRootURL: String? = environment.getProperty("application.apiRootUrl")
) {

    fun getByIds(ids: List<String>): List<Concept> {
        return ids.mapNotNull { getById(it) }
    }

    fun getById(id: String): Concept? {
        val restTemplate = RestTemplate()
        return try {
            return restTemplate.getForObject("$apiRootURL/concepts/$id", Concept::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
