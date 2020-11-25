package no.fdk.dataset_catalog.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.model.SEARCH_TYPE
import no.fdk.dataset_catalog.model.SearchRequest
import no.fdk.dataset_catalog.model.SearchResult
import no.fdk.dataset_catalog.repository.DatasetRepository
import no.fdk.dataset_catalog.security.EndpointPermissions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@Tag("unit")
class SearchServiceTest {
    private val datasetRepository: DatasetRepository = mock()
    private val datasetService: DatasetService = mock()
    private val endpointPermissions: EndpointPermissions = mock()
    private val searchService = SearchService(datasetRepository, datasetService, endpointPermissions)
    private val auth = Jwt("x", Instant.now(), Instant.now().plusSeconds(1), mapOf(Pair("a", "b")), mapOf(Pair("a", "b")))

    @Nested
    internal inner class DatasetByQuery {
        @Test
        fun `retrieves search results`() {
            val titleHits = setOf(Dataset(title = mapOf(Pair("nb", "test title"))))
            val descriptionHits = setOf(Dataset(description = mapOf(Pair("nb", "test description"))))
            val catalogId = "1"
            val query = "test"

            val request = SearchRequest(SEARCH_TYPE.DATASET_BY_QUERY, listOf(catalogId), query)

            whenever(endpointPermissions.hasOrgReadPermission(any(), any())).thenReturn(true)
            whenever(datasetRepository.findByTitleContaining(listOf(catalogId), query)).thenReturn(titleHits)
            whenever(datasetRepository.findByDescriptionContaining(listOf(catalogId), query)).thenReturn(descriptionHits)


            assertNotEquals(SearchResult(), searchService.datasetByQuery(auth, request))
        }

        @Test
        fun `returns empty list on no results`() {
            val titleHits = emptySet<Dataset>()
            val descriptionHits = emptySet<Dataset>()
            val catalogId = "1"
            val query = "test"

            val request = SearchRequest(SEARCH_TYPE.DATASET_BY_QUERY, listOf(catalogId), query)

            whenever(endpointPermissions.hasOrgReadPermission(any(), any())).thenReturn(true)
            whenever(datasetRepository.findByTitleContaining(listOf(catalogId), query)).thenReturn(titleHits)
            whenever(datasetRepository.findByDescriptionContaining(listOf(catalogId), query)).thenReturn(descriptionHits)

            assertEquals(SearchResult(), searchService.datasetByQuery(auth, request))
        }

        @Test
        fun `title hits precede description hits`() {
            val dsTitle = Dataset(title = mapOf(Pair("nb", "test title")))
            val dsDescription = Dataset(description = mapOf(Pair("nb", "test description")))
            val titleHits = setOf(dsTitle)
            val descriptionHits = setOf(dsDescription)
            val catalogId = "1"
            val query = "test"

            val request = SearchRequest(SEARCH_TYPE.DATASET_BY_QUERY, listOf(catalogId), query)

            whenever(endpointPermissions.hasOrgReadPermission(any(), any())).thenReturn(true)
            whenever(datasetRepository.findByTitleContaining(listOf(catalogId), query)).thenReturn(titleHits)
            whenever(datasetRepository.findByDescriptionContaining(listOf(catalogId), query)).thenReturn(descriptionHits)

            val result = searchService.datasetByQuery(auth, request)

            assertEquals(dsTitle, result.datasets[0])
            assertEquals(dsDescription, result.datasets[1])
        }

        @Test
        fun `exact hits precede partial hits`() {
            val dsTitle = listOf(Dataset(title = mapOf(Pair("nb", "test1"))),
                Dataset(title = mapOf(Pair("nn", "test2"))),
                Dataset(title = mapOf(Pair("nb", "test3"))),
                Dataset(title = mapOf(Pair("nb", "test"))),)
            val titleHits = dsTitle.toSet()
            val descriptionHits = emptySet<Dataset>()
            val catalogId = "1"
            val query = "test"

            val request = SearchRequest(SEARCH_TYPE.DATASET_BY_QUERY, listOf(catalogId), query)

            whenever(endpointPermissions.hasOrgReadPermission(any(), any())).thenReturn(true)
            whenever(datasetRepository.findByTitleContaining(listOf(catalogId), query)).thenReturn(titleHits)
            whenever(datasetRepository.findByDescriptionContaining(listOf(catalogId), query)).thenReturn(descriptionHits)
            val result = searchService.datasetByQuery(auth, request)

            assertEquals(dsTitle[3], result.datasets[0])
        }

        @Test
        fun `matches title on nb, nn, en`() {
            val dsTitle = listOf(Dataset(title = mapOf(Pair("nb", "test tittel"))),
                Dataset(title = mapOf(Pair("nn", "test tittel nn"))),
                Dataset(title = mapOf(Pair("nb", "test title"))))
            val titleHits = dsTitle.toSet()
            val descriptionHits = emptySet<Dataset>()
            val catalogId = "1"
            val query = "test"

            val request = SearchRequest(SEARCH_TYPE.DATASET_BY_QUERY, listOf(catalogId), query)

            whenever(endpointPermissions.hasOrgReadPermission(any(), any())).thenReturn(true)
            whenever(datasetRepository.findByTitleContaining(listOf(catalogId), query)).thenReturn(titleHits)
            whenever(datasetRepository.findByDescriptionContaining(listOf(catalogId), query)).thenReturn(descriptionHits)
            val result = searchService.datasetByQuery(auth, request)

            assertEquals(dsTitle, result.datasets)
        }

        @Test
        fun `matches description on nb, nn, en`() {
            val dsDescription = listOf(Dataset(description = mapOf(Pair("nb", "test tittel"))),
                Dataset(description = mapOf(Pair("nn", "test tittel nn"))),
                Dataset(description = mapOf(Pair("nb", "test title"))))
            val titleHits = emptySet<Dataset>()
            val descriptionHits = dsDescription.toSet()
            val catalogId = "1"
            val query = "test"

            val request = SearchRequest(SEARCH_TYPE.DATASET_BY_QUERY, listOf(catalogId), query)

            whenever(endpointPermissions.hasOrgReadPermission(any(), any())).thenReturn(true)
            whenever(datasetRepository.findByTitleContaining(listOf(catalogId), query)).thenReturn(titleHits)
            whenever(datasetRepository.findByDescriptionContaining(listOf(catalogId), query)).thenReturn(descriptionHits)
            val result = searchService.datasetByQuery(auth, request)

            assertEquals(dsDescription, result.datasets)
        }
    }

}