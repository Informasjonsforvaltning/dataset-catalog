package no.fdk.dataset_catalog.service

import com.nhaarman.mockitokotlin2.*
import no.fdk.dataset_catalog.extensions.updateSubjects
import no.fdk.dataset_catalog.model.*
import no.fdk.dataset_catalog.repository.DatasetRepository
import org.junit.jupiter.api.*
import java.lang.Exception
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Tag("unit")
class DatasetServiceTest {
    private val datasetRepository: DatasetRepository = mock()
    private val catalogService: CatalogService = mock()
    private val conceptService: ConceptService = mock()
    private val organizationService: OrganizationService = mock()
    private val publishingService: PublishingService = mock()
    private val datasetService = DatasetService(datasetRepository, catalogService, organizationService, conceptService, publishingService)

    @Nested
    internal inner class Create {
        @Test
        fun `persists new dataset`() {
            val ds = Dataset(
                "dsId",
                "catId",
                registrationStatus = REGISTRATION_STATUS.DRAFT)
            whenever(datasetRepository.save(any())).thenReturn(ds)
            whenever(catalogService.getByID("catId")).thenReturn(Catalog("catId"))
            val actual = datasetService.create("catId", ds)
            assertEquals(ds.copy(lastModified = actual?.lastModified, uri = actual?.uri), actual)
        }
    }


    @Nested
    internal inner class GetById {
        @Test
        fun `gets by id`() {
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(Dataset("dsId", "catId")))
            val dataset = datasetService.getByID("catId","dsId")
            assertNotNull(dataset)
        }

        @Test
        fun `cannot get non-existing dataset`() {
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.empty())
            val dataset = datasetService.getByID("catId","dsId")
            assertNull(dataset)
        }

        @Test
        fun `cannot get dataset in other catalog`() {
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.empty())
            val dataset = datasetService.getByID("other-catId","dsId")
            assertNull(dataset)
        }
    }


    @Nested
    internal inner class GetAll {
        @Test
        fun `getAll returns all datasets in catalog`() {
            val expected = listOf(Dataset("1","1"), Dataset("2","1"))
            whenever(datasetRepository.findByCatalogId("1")).thenReturn(expected)
            val actual = datasetService.getAll("1")
            assertEquals(expected, actual)
        }
    }


    @Nested
    internal inner class Delete {
        @Test
        fun `delete successfully removes dataset`() {
            val ds = Dataset("dsId", "catId")
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            assertDoesNotThrow { datasetService.delete("catId","dsId") }
        }

        @Test
        fun `delete non-existing dataset throws exception`() {
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.empty())
            assertThrows<Exception> { datasetService.delete("catId","dsId") }
        }
    }


    @Nested
    internal inner class Update {
        @Test
        fun `update persists changes to dataset`() {
            val ds = Dataset("dsId", "catId")
            val expected = Dataset("dsId", "catId", uri="test")
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(datasetRepository.save(any())).thenReturn(expected)
            whenever(catalogService.getByID("catId")).thenReturn(Catalog())
            val actual = datasetService.updateDataset("catId","dsId", expected)

            assertEquals(expected.copy( lastModified = actual?.lastModified), actual)
        }
    }


    @Nested
    internal inner class UpdateConcepts {
        @Test
        fun `updates concepts`() {
            val ds = Dataset("dsId", "catId", concepts = listOf(Concept("1")))
            val expected = Dataset("dsId", "catId", concepts = listOf(Concept("1", uri="uri")))
            whenever(conceptService.getConcepts(listOf("1"))).thenReturn(listOf(Concept("1", uri="uri")))
            val actual = with(datasetService) {ds.updateConcepts()}
            assertEquals(expected, actual)
        }
    }


    @Nested
    internal inner class UpdateSubject {
        @Test
        fun `updates subject`() {
            val ds = Dataset("dsId", "catId", concepts = listOf(Concept("1", "uri")))
            val expected = ds.copy(subject = listOf(Subject(id = "1", uri = "uri")))
            val actual = ds.updateSubjects()
            assertEquals(expected, actual)
        }
    }

    @Nested
    internal inner class TriggerHarvest {
        @Test
        fun `triggers harvest on update to published dataset`() {
            val cat = Catalog("catId", publisher = Publisher(id="pubId"), hasPublishedDataSource = true)
            val ds = Dataset("dsId", "catId", registrationStatus = REGISTRATION_STATUS.PUBLISH)

            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(datasetRepository.save(any())).thenReturn(ds)
            whenever(catalogService.getByID("catId")).thenReturn(cat)

            datasetService.updateDataset("catId","dsId", ds)

            verify(publishingService, times(1)).triggerHarvest("dsId", "catId", "pubId")
        }

        @Test
        fun `does not trigger harvest on update to draft dataset`() {
            val cat = Catalog("catId", publisher = Publisher(id="pubId"), hasPublishedDataSource = true)
            val ds = Dataset("dsId", "catId", registrationStatus = REGISTRATION_STATUS.DRAFT)

            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(datasetRepository.save(any())).thenReturn(ds)
            whenever(catalogService.getByID("catId")).thenReturn(cat)

            datasetService.updateDataset("catId","dsId", ds)

            verify(publishingService, times(0)).triggerHarvest("dsId", "catId", "pubId")
        }

        @Test
        fun `adds datasource on first published dataset in catalog`() {
            val cat = Catalog("catId", publisher = Publisher(id="pubId"), hasPublishedDataSource = false)
            val ds = Dataset("dsId", "catId", registrationStatus = REGISTRATION_STATUS.DRAFT)

            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(datasetRepository.save(any())).thenReturn(ds)
            whenever(catalogService.getByID("catId")).thenReturn(cat)

            datasetService.create("catId", ds)
            datasetService.updateDataset("catId","dsId", ds.copy(registrationStatus = REGISTRATION_STATUS.PUBLISH))

            verify(catalogService, times(1)).addDataSource(cat)
        }

        @Test
        fun `does not add datasource on already added catalog`() {
            val cat = Catalog("catId", publisher = Publisher(id="pubId"), hasPublishedDataSource = true)
            val ds = Dataset("dsId", "catId", registrationStatus = REGISTRATION_STATUS.DRAFT)

            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(datasetRepository.save(any())).thenReturn(ds)
            whenever(catalogService.getByID("catId")).thenReturn(cat)

            datasetService.create("catId", ds)
            datasetService.updateDataset("catId","dsId", ds.copy(registrationStatus = REGISTRATION_STATUS.PUBLISH))

            verify(catalogService, times(0)).addDataSource(cat)
        }

        @Test
        fun `triggers harvest and adds datasource on first published dataset in catalog`() {
            val cat = Catalog("catId", publisher = Publisher(id="pubId"), hasPublishedDataSource = false)
            val ds = Dataset("dsId", "catId", registrationStatus = REGISTRATION_STATUS.PUBLISH)

            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(datasetRepository.save(any())).thenReturn(ds)
            whenever(catalogService.getByID("catId")).thenReturn(cat)

            datasetService.updateDataset("catId","dsId", ds)

            verify(publishingService, times(1)).triggerHarvest("dsId", "catId", "pubId")
            verify(catalogService, times(1)).addDataSource(cat)
        }
    }

}