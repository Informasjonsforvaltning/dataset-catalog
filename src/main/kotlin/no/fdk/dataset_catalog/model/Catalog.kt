package no.fdk.dataset_catalog.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document( collection = "catalogs")
data class Catalog(
    @Id
    val id: String,
    val uri: String? = null,
    val title: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap(),
    val publisher: Publisher? = null,
    val issued: Long? = null,
    val modified: Long? = null,
    val language: String? = null,
    val themeTaxonomy: List<String> = emptyList(),
    val dataset: List<Dataset> = emptyList(),
)
