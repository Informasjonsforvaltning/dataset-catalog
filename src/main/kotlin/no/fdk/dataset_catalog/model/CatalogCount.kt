package no.fdk.dataset_catalog.model

import com.fasterxml.jackson.annotation.JsonInclude

data class CatalogDTO(
    val _embedded: Map<String, List<CatalogCount>>?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CatalogCount(
    val id: String,
    val datasetCount: Long,
)
