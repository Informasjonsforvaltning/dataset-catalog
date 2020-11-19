package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.exception.NotPermittedException
import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.model.SearchRequest
import no.fdk.dataset_catalog.model.SearchResult
import no.fdk.dataset_catalog.repository.DatasetRepository
import no.fdk.dataset_catalog.security.EndpointPermissions
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.util.*

private val logger = LoggerFactory.getLogger(SearchService::class.java)

@Service
class SearchService (
    private val datasetRepository: DatasetRepository,
    private val datasetService: DatasetService,
    private val endpointPermissions: EndpointPermissions) {

    fun datasetByQuery(jwt: Jwt, searchRequest: SearchRequest): SearchResult =
        if (searchRequest.catalogIDs.all { endpointPermissions.hasOrgReadPermission(jwt, it) }) {
             logger.info("Searching for datasets with title or description containing [${searchRequest.query}]")

            if (searchRequest.query == "") {
                SearchResult(
                    datasets = searchRequest.catalogIDs.map { datasetService.getAll(it) }.flatten()
                )
            } else {
                val queryString = "\\b${searchRequest.query.toLowerCase()}"
                val titleHits = datasetRepository.findByTitleContaining(searchRequest.catalogIDs, queryString)
                val descriptionHits = datasetRepository.findByDescriptionContaining(searchRequest.catalogIDs, queryString)
                SearchResult(
                    datasets = orderDatasetSearch(titleHits.union(descriptionHits), searchRequest.query)
                )
            }
        } else {
            throw NotPermittedException("Catalog access denied.")
        }


    private fun orderDatasetSearch(results: Set<Dataset>, query: String): List<Dataset> {
        val orderedResults = LinkedList<Dataset>()
        val queryLC = query.toLowerCase()
        results.map {
            if (queryLC == it.title?.get("nb")?.toLowerCase() ||
                queryLC == it.title?.get("nn")?.toLowerCase() ||
                queryLC == it.title?.get("en")?.toLowerCase()) {
                orderedResults.addFirst(it)
            } else orderedResults.addLast(it)
        }

        return orderedResults.toList()
    }
}