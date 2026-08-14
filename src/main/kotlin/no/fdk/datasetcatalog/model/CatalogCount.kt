package no.fdk.datasetcatalog.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CatalogCount(val id: String, val datasetCount: Long)
