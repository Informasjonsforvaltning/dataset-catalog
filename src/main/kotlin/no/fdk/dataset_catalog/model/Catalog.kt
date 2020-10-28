package no.fdk.dataset_catalog.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document( collection = "catalogs")
data class Catalog(
    @Id
    val id: String? = null,
    val uri: String? = null,
    val title: Map<String, String>? = null,
    val description: Map<String, String>? = null,
    val publisher: Publisher? = null,
    val issued: Long? = null,
    val modified: Long? = null,
    val language: String? = null,
    val themeTaxonomy: List<String>? = null,
    val dataset: List<Dataset>? = null,
)
