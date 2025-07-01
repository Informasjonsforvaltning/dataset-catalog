package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.model.CatalogCount
import no.fdk.dataset_catalog.repository.DatasetOperations
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.test.context.TestPropertySource
import kotlin.test.assertEquals
import kotlin.test.assertNull

@TestPropertySource(properties = ["application.openDataEnhet=test"])

@Tag("unit")
class CatalogServiceTest {
    private val datasetOperations: DatasetOperations = mock()
    private val catalogService = CatalogService(datasetOperations)

    @Nested
    internal inner class GetById{
        @Test
        fun `non empty catalog when there exists datasets with queried catalogId`() {
            whenever(datasetOperations.datasetCountForCatalogs(listOf("existing-id")))
                .thenReturn(listOf(CatalogCount("existing-id", 1)))
            val expected = CatalogCount("existing-id", 1)
            val catalog = catalogService.getByID("existing-id")
            assertEquals(expected,catalog)
        }

        @Test
        fun `no catalog created when no datasets exists with corresponding catalogId`() {
            whenever(datasetOperations.datasetCountForCatalogs(listOf("non-existing-id")))
                .thenReturn(emptyList())
            val catalog = catalogService.getByID("non-existing-id")
            assertNull(catalog)
        }
    }

    @Nested
    internal inner class GetAll{
       @Test
        fun `getAll returns all catalogs`() {
            whenever(datasetOperations.getAllCatalogIds()).thenReturn(listOf("0", "1", "2"))
           whenever(datasetOperations.datasetCountForCatalogs(listOf("0", "1", "2")))
               .thenReturn(listOf(
                   CatalogCount("0", 10),
                   CatalogCount("1", 20),
                   CatalogCount("2", 30))
               )

            val expected = listOf(
                CatalogCount("0", 10),
                CatalogCount("1", 20),
                CatalogCount("2", 30)
            )
            val result = catalogService.getAll()
            assertEquals(expected, result)
        }
    }

}
