package no.fdk.dataset_catalog.service

import net.bytebuddy.asm.Advice.Local
import no.fdk.dataset_catalog.extensions.datasetToDBO
import no.fdk.dataset_catalog.model.*
import no.fdk.dataset_catalog.repository.DatasetRepository
import no.fdk.dataset_catalog.security.EndpointPermissions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
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
    private val auth =
        Jwt("x", Instant.now(), Instant.now().plusSeconds(1), mapOf(Pair("a", "b")), mapOf(Pair("a", "b")))

    @Nested
    internal inner class DatasetByQuery {
        @Test
        fun `retrieves search results`() {
            val titleHits = setOf(DatasetDBO(id="1", catalogId = "345", title = LocalizedStrings("nb", "test title")))
            val descriptionHits = setOf(DatasetDBO(id="1", catalogId = "345", description = LocalizedStrings("nb", "test description")))
            val catalogId = "1"
            val query = "test"

            val request = SearchRequest(SEARCH_TYPE.DATASET_BY_QUERY, listOf(catalogId), query)

            whenever(endpointPermissions.hasOrgReadPermission(any(), any())).thenReturn(true)
            whenever(datasetRepository.findByTitleContaining(listOf(catalogId), query)).thenReturn(titleHits)
            whenever(
                datasetRepository.findByDescriptionContaining(
                    listOf(catalogId),
                    query
                )
            ).thenReturn(descriptionHits)


            assertNotEquals(SearchResult(), searchService.datasetByQuery(auth, request))
        }

        @Test
        fun `returns empty list on no results`() {
            val titleHits = emptySet<DatasetDBO>()
            val descriptionHits = emptySet<DatasetDBO>()
            val catalogId = "1"
            val query = "test"

            val request = SearchRequest(SEARCH_TYPE.DATASET_BY_QUERY, listOf(catalogId), query)

            whenever(endpointPermissions.hasOrgReadPermission(any(), any())).thenReturn(true)
            whenever(datasetRepository.findByTitleContaining(listOf(catalogId), query)).thenReturn(titleHits)
            whenever(
                datasetRepository.findByDescriptionContaining(
                    listOf(catalogId),
                    query
                )
            ).thenReturn(descriptionHits)

            assertEquals(SearchResult(), searchService.datasetByQuery(auth, request))
        }

        @Test
        fun `title hits precede description hits`() {
            val dsTitle = DatasetDBO(id="1", catalogId = "345", title = LocalizedStrings("nb", "test title"))
            val dsDescription = DatasetDBO(id="1", catalogId = "345", description = LocalizedStrings("nb", "test description"))
            val titleHits = setOf(dsTitle)
            val descriptionHits = setOf(dsDescription)
            val catalogId = "1"
            val query = "test"

            val request = SearchRequest(SEARCH_TYPE.DATASET_BY_QUERY, listOf(catalogId), query)

            whenever(endpointPermissions.hasOrgReadPermission(any(), any())).thenReturn(true)
            whenever(datasetRepository.findByTitleContaining(listOf(catalogId), query)).thenReturn(titleHits)
            whenever(
                datasetRepository.findByDescriptionContaining(
                    listOf(catalogId),
                    query
                )
            ).thenReturn(descriptionHits)

            val result = searchService.datasetByQuery(auth, request)

            assertEquals(dsTitle, result.datasets[0].datasetToDBO())
            assertEquals(dsDescription, result.datasets[1].datasetToDBO())
        }

        @Test
        fun `exact hits precede partial hits`() {
            val dsTitle = listOf(
                DatasetDBO(id="1", catalogId = "1234", title = LocalizedStrings("nb", "test1")),
                DatasetDBO(id="2", catalogId = "1234", title = LocalizedStrings("nn", "test2")),
                DatasetDBO(id="3", catalogId = "1234", title = LocalizedStrings("nb", "test3")),
                DatasetDBO(id="4", catalogId = "1234", title = LocalizedStrings("nb", "test")),
            )
            val titleHits = dsTitle.toSet()
            val descriptionHits = emptySet<DatasetDBO>()
            val catalogId = "1"
            val query = "test"

            val request = SearchRequest(SEARCH_TYPE.DATASET_BY_QUERY, listOf(catalogId), query)

            whenever(endpointPermissions.hasOrgReadPermission(any(), any())).thenReturn(true)
            whenever(datasetRepository.findByTitleContaining(listOf(catalogId), query)).thenReturn(titleHits)
            whenever(
                datasetRepository.findByDescriptionContaining(
                    listOf(catalogId),
                    query
                )
            ).thenReturn(descriptionHits)
            val result = searchService.datasetByQuery(auth, request)

            assertEquals(dsTitle[3], result.datasets[0].datasetToDBO())
        }

        @Test
        fun `matches title on nb, nn, en`() {
            val dsTitle = listOf(
                DatasetDBO(id="1", catalogId = "1234", title = LocalizedStrings("nb", "test tittel")),
                DatasetDBO(id="1", catalogId = "1234", title = LocalizedStrings("nn", "test tittel nn")),
                DatasetDBO(id="1", catalogId = "1234", title = LocalizedStrings("nb", "test title"))
            )
            val titleHits = dsTitle.toSet()
            val descriptionHits = emptySet<DatasetDBO>()
            val catalogId = "1"
            val query = "test"

            val request = SearchRequest(SEARCH_TYPE.DATASET_BY_QUERY, listOf(catalogId), query)

            whenever(endpointPermissions.hasOrgReadPermission(any(), any())).thenReturn(true)
            whenever(datasetRepository.findByTitleContaining(listOf(catalogId), query)).thenReturn(titleHits)
            whenever(
                datasetRepository.findByDescriptionContaining(
                    listOf(catalogId),
                    query
                )
            ).thenReturn(descriptionHits)
            val result = searchService.datasetByQuery(auth, request)

            assertEquals(dsTitle, result.datasets.map { dataset -> dataset.datasetToDBO() })
        }

        @Test
        fun `matches description on nb, nn, en`() {
            val dsDescription = listOf(
                DatasetDBO(id="1", catalogId = "1234", description = LocalizedStrings("nb", "test tittel")),
                DatasetDBO(id="1", catalogId = "1234",description = LocalizedStrings("nn", "test tittel nn")),
                DatasetDBO(id="1", catalogId = "1234",description = LocalizedStrings("nb", "test title"))
            )
            val titleHits = emptySet<DatasetDBO>()
            val descriptionHits = dsDescription.toSet()
            val catalogId = "1"
            val query = "test"

            val request = SearchRequest(SEARCH_TYPE.DATASET_BY_QUERY, listOf(catalogId), query)

            whenever(endpointPermissions.hasOrgReadPermission(any(), any())).thenReturn(true)
            whenever(datasetRepository.findByTitleContaining(listOf(catalogId), query)).thenReturn(titleHits)
            whenever(
                datasetRepository.findByDescriptionContaining(
                    listOf(catalogId),
                    query
                )
            ).thenReturn(descriptionHits)
            val result = searchService.datasetByQuery(auth, request)

            assertEquals(dsDescription, result.datasets.map { dataset -> dataset.datasetToDBO() })
        }
    }

}