package no.fdk.dataset_catalog.service

import com.nhaarman.mockitokotlin2.*
import no.fdk.dataset_catalog.configuration.ApplicationProperties
import no.fdk.dataset_catalog.extensions.updateUriIfNeeded
import no.fdk.dataset_catalog.model.Catalog
import no.fdk.dataset_catalog.repository.CatalogRepository
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.test.context.TestPropertySource
import java.lang.Exception
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@TestPropertySource(properties = ["application.openDataEnhet=test"])

@Tag("unit")
class CatalogServiceTest {
    private val catalogRepository: CatalogRepository = mock()
    private val organizationService: OrganizationService = mock()
    private val publishingService: PublishingService = mock()
    private val applicationProperties = ApplicationProperties(
        "localhost:5000",
        "localhost:5000",
        "localhost:5000",
        0L,
    "catalogHarvestRoute",
    "newDataSourceRoute",
    "harvests")
    private val catalogService = CatalogService(catalogRepository, organizationService, publishingService, applicationProperties)

    @Nested
    internal inner class Create{
        @Test
        fun `persists new catalog`() {
            catalogService.create(Catalog("1"))

            verify(catalogRepository, times(1)).save(any())
        }
    }

    @Nested
    internal inner class GetById{
        @Test
        fun `gets by id`() {
            whenever(catalogRepository.findById("non-existing-id")).thenReturn(Optional.of(Catalog("non-existing-id")))
            val dataset = catalogService.getByID("non-existing-id")
            assertNotNull(dataset)
        }

        @Test
        fun `cannot get non-existing catalog`() {
            whenever(catalogRepository.findById("non-existing-id")).thenReturn(Optional.empty())
            val dataset = catalogService.getByID("non-existing-id")
            assertNull(dataset)
        }
    }

    @Nested
    internal inner class GetAll{
        @Test
        fun `getAll returns all catalogs`() {
            val datasets = listOf(Catalog("1"), Catalog("2"))
            whenever(catalogRepository.findAll()).thenReturn(datasets)
            val result = catalogService.getAll()
            assertEquals(datasets, result)
        }
    }

    @Nested
    internal inner class CreateCatalogsIfNeeded{
        @Test
        fun `creates catalogs for adminable organization numbers`() {
            val adminableOrgs: Set<String> = setOf("1")
            whenever(catalogRepository.findById(any())).thenReturn(Optional.empty())
            whenever(catalogRepository.save(any())).thenReturn(null)
            catalogService.createCatalogsIfNeeded(adminableOrgs)

            verify(catalogRepository, times(1)).save(any())
        }

        @Test
        fun `does not create for already existing catalogs`() {
            val adminableOrgs: Set<String> = setOf("1")
            val cat = Catalog("1")
            whenever(catalogRepository.findById(any())).thenReturn(Optional.of(cat))
            whenever(catalogRepository.save(any())).thenReturn(null)
            catalogService.createCatalogsIfNeeded(adminableOrgs)

            verify(catalogRepository, times(0)).save(any())
        }
    }

    @Nested
    internal inner class Delete{
        @Test
        fun `delete successfully removes catalog`() {
            whenever(catalogRepository.findById("1")).thenReturn(Optional.of(Catalog()))
            catalogService.delete("1")
            verify(catalogRepository, times(1)).delete(Catalog())
        }

        @Test
        fun `deletion of non-existing catalog throws exception`() {
            whenever(catalogRepository.findById("1")).thenReturn(Optional.empty())
            assertThrows<Exception> { catalogService.delete("1") }
        }
    }

    @Nested
    internal inner class Update{
        @Test
        fun `update persists changes to dataset`() {
            val cat = Catalog("catId")
            val new = cat.copy("catId", uri="test uri")
            val expected = with(catalogService) { new
                .copy()
                .updatePublisherIfNeeded()
                .updateUriIfNeeded(applicationProperties.catalogUriHost) }
            whenever(catalogRepository.findById("catId")).thenReturn(Optional.of(cat))
            whenever(catalogRepository.save(expected)).thenReturn(expected)
            val actual = catalogService.update("catId", new)

            assertEquals(expected, actual)
        }
    }


}
