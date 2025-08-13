package no.fdk.dataset_catalog.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CatalogCount(
    val id: String,
    val datasetCount: Long,
)
