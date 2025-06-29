package no.fdk.dataset_catalog.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.fdk.dataset_catalog.configuration.ApplicationProperties
import no.fdk.dataset_catalog.model.*
import no.fdk.dataset_catalog.repository.DatasetRepository
import no.fdk.dataset_catalog.utils.TEST_DATASET_1
import org.junit.jupiter.api.*
import org.mockito.kotlin.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class DatasetServiceTest {
    private val datasetRepository: DatasetRepository = mock()
    private val catalogService: CatalogService = mock()
    private val organizationService: OrganizationService = mock()
    private val publishingService: PublishingService = mock()
    private val applicationProperties: ApplicationProperties = mock()
    private val datasetService = DatasetService(
        datasetRepository, catalogService, organizationService,
        publishingService, applicationProperties, jacksonObjectMapper()
    )

    @Nested
    internal inner class Create {
        @Test
        fun `persists new dataset`() {
            val ds = Dataset(
                "dsId",
                "catId",
                specializedType = SpecializedType.SERIES,
                registrationStatus = REGISTRATION_STATUS.DRAFT
            )
            whenever(catalogService.getByID("catId")).thenReturn(Catalog("catId"))
            datasetService.create("catId", ds)
            argumentCaptor<List<Dataset>>().apply {
                verify(datasetRepository, times(1)).saveAll(capture())
                assertTrue(firstValue.size == 1)
                val actual = firstValue.first()
                assertEquals(ds.copy(lastModified = actual.lastModified, uri = actual.uri), actual)
            }
        }
    }


    @Nested
    internal inner class GetById {
        @Test
        fun `gets by id`() {
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(Dataset("dsId", "catId")))
            val dataset = datasetService.getByID("catId", "dsId")
            assertNotNull(dataset)
        }

        @Test
        fun `cannot get non-existing dataset`() {
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.empty())
            val dataset = datasetService.getByID("catId", "dsId")
            assertNull(dataset)
        }

        @Test
        fun `cannot get dataset in other catalog`() {
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.empty())
            val dataset = datasetService.getByID("other-catId", "dsId")
            assertNull(dataset)
        }
    }


    @Nested
    internal inner class GetAll {
        @Test
        fun `getAll returns all datasets in catalog`() {
            val expected = listOf(Dataset("1", "1"), Dataset("2", "1"))
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
            assertDoesNotThrow { datasetService.delete("catId", "dsId") }
        }

        @Test
        fun `delete non-existing dataset throws exception`() {
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.empty())
            assertThrows<Exception> { datasetService.delete("catId", "dsId") }
        }
    }

    @Nested
    internal inner class Update {
        @Test
        fun `update dataset with add operation`() {
            val ds = Dataset("dsId", "catId")
            val expected = Dataset("dsId", "catId", source = "test")
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(catalogService.getByID("catId")).thenReturn(Catalog())
            datasetService.updateDataset("catId", "dsId", listOf(JsonPatchOperation(OpEnum.ADD, "/source", "test")))

            argumentCaptor<List<Dataset>>().apply {
                verify(datasetRepository, times(1)).saveAll(capture())
                assertTrue(firstValue.size == 1)
                assertEquals(expected.copy(lastModified = firstValue.first().lastModified), firstValue.first())
            }
        }

        @Test
        fun `update dataset with replace operation`() {
            val ds = Dataset(
                "dsId",
                "catId",
                temporal = listOf(
                    PeriodOfTime(
                        "period-id",
                        "period",
                        LocalDate.of(2020, 11, 11),
                        LocalDate.of(2021, 4, 4)
                    )
                )
            )
            val expected = Dataset(
                "dsId",
                "catId",
                temporal = listOf(
                    PeriodOfTime(
                        "period-id",
                        "period",
                        LocalDate.of(2020, 10, 10),
                        LocalDate.of(2021, 4, 4)
                    )
                )
            )
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(catalogService.getByID("catId")).thenReturn(Catalog())
            datasetService.updateDataset(
                "catId",
                "dsId",
                listOf(JsonPatchOperation(OpEnum.REPLACE, "/temporal/0/startDate", "2020-10-10"))
            )

            argumentCaptor<List<Dataset>>().apply {
                verify(datasetRepository, times(1)).saveAll(capture())
                assertTrue(firstValue.size == 1)
                assertEquals(expected.copy(lastModified = firstValue.first().lastModified), firstValue.first())
            }
        }

        @Test
        fun `update dataset with copy operation`() {
            val ds = Dataset("dsId", "catId", title = mapOf(Pair("nb", "tittel")))
            val expected = Dataset("dsId", "catId", title = mapOf(Pair("nb", "tittel"), Pair("nn", "tittel")))
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(catalogService.getByID("catId")).thenReturn(Catalog())
            datasetService.updateDataset(
                "catId",
                "dsId",
                listOf(JsonPatchOperation(OpEnum.COPY, "/title/nn", null, "/title/nb"))
            )

            argumentCaptor<List<Dataset>>().apply {
                verify(datasetRepository, times(1)).saveAll(capture())
                assertTrue(firstValue.size == 1)
                assertEquals(expected.copy(lastModified = firstValue.first().lastModified), firstValue.first())
            }
        }

        @Test
        fun `update dataset with move operation`() {
            val ds = Dataset("dsId", "catId", title = mapOf(Pair("nb", "beskrivelse")), description = null)
            val expected = Dataset("dsId", "catId", title = null, description = mapOf(Pair("nb", "beskrivelse")))
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(catalogService.getByID("catId")).thenReturn(Catalog())
            datasetService.updateDataset(
                "catId",
                "dsId",
                listOf(JsonPatchOperation(OpEnum.MOVE, "/description", null, "/title"))
            )

            argumentCaptor<List<Dataset>>().apply {
                verify(datasetRepository, times(1)).saveAll(capture())
                assertTrue(firstValue.size == 1)
                assertEquals(expected.copy(lastModified = firstValue.first().lastModified), firstValue.first())
            }
        }

        @Test
        fun `update dataset with remove operation`() {
            val ds = Dataset("dsId", "catId", source = "test")
            val expected = Dataset("dsId", "catId")
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(catalogService.getByID("catId")).thenReturn(Catalog())
            datasetService.updateDataset("catId", "dsId", listOf(JsonPatchOperation(OpEnum.REMOVE, "/source")))

            argumentCaptor<List<Dataset>>().apply {
                verify(datasetRepository, times(1)).saveAll(capture())
                assertTrue(firstValue.size == 1)
                assertEquals(expected.copy(lastModified = firstValue.first().lastModified), firstValue.first())
            }
        }

        @Test
        fun `update of specialized type is ignored`() {
            val ds = Dataset("dsId", "catId")
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(catalogService.getByID("catId")).thenReturn(Catalog())
            assertThrows<ResponseStatusException> {
                datasetService.updateDataset(
                    "catId",
                    "dsId",
                    listOf(JsonPatchOperation(OpEnum.ADD, "/specializedType", "SERIES"))
                )
            }
            argumentCaptor<List<Dataset>>().apply {
                verify(datasetRepository, times(0)).saveAll(capture())
            }
        }
    }

    @Nested
    internal inner class TriggerHarvest {
        @Test
        fun `triggers harvest on update to published dataset`() {
            val cat = Catalog("catId", publisher = Publisher(id = "pubId"), hasPublishedDataSource = true)
            val ds = Dataset("dsId", "catId", registrationStatus = REGISTRATION_STATUS.PUBLISH)

            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(datasetRepository.save(any())).thenReturn(ds)
            whenever(catalogService.getByID("catId")).thenReturn(cat)

            datasetService.updateDataset("catId", "dsId", emptyList())

            verify(publishingService, times(1)).triggerHarvest("catId", "pubId")
        }

        @Test
        fun `does not trigger harvest on update to draft dataset`() {
            val cat = Catalog("catId", publisher = Publisher(id = "pubId"), hasPublishedDataSource = true)
            val ds = Dataset("dsId", "catId", registrationStatus = REGISTRATION_STATUS.DRAFT)

            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(datasetRepository.save(any())).thenReturn(ds)
            whenever(catalogService.getByID("catId")).thenReturn(cat)

            datasetService.updateDataset("catId", "dsId", listOf(JsonPatchOperation(OpEnum.ADD, "/source", "hei")))

            verify(publishingService, times(0)).triggerHarvest("catId", "pubId")
        }

        @Test
        fun `adds datasource on first published dataset in catalog`() {
            val cat = Catalog("catId", publisher = Publisher(id = "pubId"), hasPublishedDataSource = false)
            val ds = Dataset("dsId", "catId", registrationStatus = REGISTRATION_STATUS.DRAFT)

            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(datasetRepository.save(any())).thenReturn(ds)
            whenever(catalogService.getByID("catId")).thenReturn(cat)

            datasetService.create("catId", ds)
            datasetService.updateDataset(
                "catId",
                "dsId",
                listOf(JsonPatchOperation(OpEnum.REPLACE, "/registrationStatus", REGISTRATION_STATUS.PUBLISH))
            )

            verify(catalogService, times(1)).addDataSource(cat)
        }

        @Test
        fun `does not add datasource on already added catalog`() {
            val cat = Catalog("catId", publisher = Publisher(id = "pubId"), hasPublishedDataSource = true)
            val ds = Dataset("dsId", "catId", registrationStatus = REGISTRATION_STATUS.DRAFT)

            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(datasetRepository.save(any())).thenReturn(ds)
            whenever(catalogService.getByID("catId")).thenReturn(cat)

            datasetService.create("catId", ds)
            datasetService.updateDataset(
                "catId",
                "dsId",
                listOf(JsonPatchOperation(OpEnum.REPLACE, "/registrationStatus", REGISTRATION_STATUS.PUBLISH))
            )

            verify(catalogService, times(0)).addDataSource(cat)
        }

        @Test
        fun `triggers harvest and adds datasource on first published dataset in catalog`() {
            val cat = Catalog("catId", publisher = Publisher(id = "pubId"), hasPublishedDataSource = false)
            val ds = Dataset("dsId", "catId", registrationStatus = REGISTRATION_STATUS.PUBLISH)

            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(datasetRepository.save(any())).thenReturn(ds)
            whenever(catalogService.getByID("catId")).thenReturn(cat)

            datasetService.updateDataset("catId", "dsId", emptyList())

            verify(publishingService, times(1)).triggerHarvest("catId", "pubId")
            verify(catalogService, times(1)).addDataSource(cat)
        }
    }

    @Nested
    internal inner class Resolve {

        @Test
        fun `Resolves references`() {
            val dataset = TEST_DATASET_1
            val referencedDataset = Dataset(originalUri = "http://originaluri/resolved")

            val resolved = dataset.references?.map {
                it.copy(
                    source = SkosConcept("http://originaluri/resolved", prefLabel = it.source?.prefLabel)
                )
            }

            whenever(applicationProperties.catalogUriHost).thenReturn("http://mycatalog")
            whenever(datasetRepository.findById("1")).thenReturn(Optional.of(referencedDataset))

            assertEquals(resolved, datasetService.resolveReferences(dataset))
        }
    }
}
