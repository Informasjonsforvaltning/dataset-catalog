package no.fdk.dataset_catalog.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Subject (
    val uri: String? = null,
    val definition: Map<String, String>? = null,
    val prefLabel: Map<String, String>? = null,
    val id: String? = null,
    val identifier: String? = null,
    val altLabel: List<Map<String, String>>? = null,
    val note: Map<String, String>? = null,
    val source: String? = null,
    val creator: Publisher? = null,
    val inScheme: List<String>? = null,
    val datasets: List<Dataset>? = null
)
