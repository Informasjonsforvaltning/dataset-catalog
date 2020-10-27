package no.fdk.dataset_catalog.model

data class Subject (
    val uri: String? = null,
    val definition: Map<String, String> = emptyMap(),
    val prefLabel: Map<String, String> = emptyMap(),
    val id: String? = null,
    val identifier: String? = null,
    val altLabel: List<Map<String, String>?> = emptyList(),
    val note: Map<String, String>? = null,
    val source: String? = null,
    val creator: Publisher? = null,
    val inScheme: List<String>? = null,
    val datasets: List<Dataset>? = null
)
