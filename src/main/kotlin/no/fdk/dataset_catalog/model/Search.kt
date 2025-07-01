package no.fdk.dataset_catalog.model

data class SearchResult(
    val datasets: List<Dataset> = emptyList(),
    val catalogs: List<CatalogCount> = emptyList()
)

enum class SEARCH_TYPE {
    DATASET_BY_QUERY
}

data class SearchRequest (
    val searchType: SEARCH_TYPE,
    val catalogIDs: List<String> = emptyList(),
    val query: String = ""
)
