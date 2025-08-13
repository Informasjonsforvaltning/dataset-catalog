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
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class DatasetServiceTest {
    private val datasetRepository: DatasetRepository = mock()
    private val publishingService: PublishingService = mock()
    private val applicationProperties: ApplicationProperties = mock()
    private val datasetService = DatasetService(
        datasetRepository, publishingService, applicationProperties, jacksonObjectMapper()
    )

    @Nested
    internal inner class Create {
        @Test
        fun `persists new dataset`() {
            val ds = DatasetToCreate(
                specializedType = SpecializedType.SERIES,
            )
            datasetService.createDataset("catId", ds)
            argumentCaptor<List<DatasetDBO>>().apply {
                verify(datasetRepository, times(1)).saveAll(capture())
                assertTrue(firstValue.size == 1)
                val actual = firstValue.first()
                assertEquals(actual.copy(specializedType = ds.specializedType), actual)
            }
        }
    }

    @Nested
    internal inner class GetById {
        @Test
        fun `gets by id`() {
            whenever(datasetRepository.findById("dsId")).thenReturn(
                Optional.of(
                    DatasetDBO(
                        "dsId",
                        "catId",
                        uri = "uri",
                        lastModified = LocalDateTime.now()
                    )
                )
            )
            val dataset = datasetService.getDatasetByID("catId", "dsId")
            assertNotNull(dataset)
        }

        @Test
        fun `cannot get non-existing dataset`() {
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.empty())
            val dataset = datasetService.getDatasetByID("catId", "dsId")
            assertNull(dataset)
        }

        @Test
        fun `cannot get dataset in other catalog`() {
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.empty())
            val dataset = datasetService.getDatasetByID("other-catId", "dsId")
            assertNull(dataset)
        }
    }

    @Nested
    internal inner class GetAll {
        @Test
        fun `getAll returns all datasets in catalog`() {
            val expected = listOf(
                DatasetDBO("1", "1", lastModified = LocalDateTime.now(), uri = null, published = false, approved = false),
                DatasetDBO("2", "1", lastModified = LocalDateTime.now(), uri = null, published = false, approved = false)
            )
            whenever(datasetRepository.findByCatalogId("1")).thenReturn(expected)
            val actual = datasetService.getAllDatasets("1").map { dataset -> dataset }
            assertEquals(expected, actual)
        }
    }

    @Nested
    internal inner class Delete {
        @Test
        fun `delete successfully removes dataset`() {
            val ds = DatasetDBO("dsId", "catId", uri = "uri", lastModified = LocalDateTime.now())
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
            val ds = DatasetDBO("dsId", "catId", uri = "uri", lastModified = null)
            val expected = DatasetDBO("dsId", "catId", uri = "uri", lastModified = null, type = "test")
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))

            datasetService.updateDatasetDBO("catId", "dsId", listOf(JsonPatchOperation(OpEnum.ADD, "/type", "test")))

            argumentCaptor<List<DatasetDBO>>().apply {
                verify(datasetRepository, times(1)).saveAll(capture())
                assertTrue(firstValue.size == 1)
                assertEquals(
                    expected.copy(lastModified = firstValue.first().lastModified),
                    firstValue.first()
                )
            }
        }

        @Test
        fun `update dataset with replace operation`() {
            val ds = DatasetDBO(
                "dsId",
                "catId",
                uri = "uri",
                lastModified = LocalDateTime.now(),
                temporal = listOf(
                    PeriodOfTimeDBO(
                        LocalDate.of(2020, 11, 11),
                        LocalDate.of(2021, 4, 4)
                    )
                )
            )
            val expected = DatasetDBO(
                "dsId",
                "catId", uri = "uri",
                lastModified = LocalDateTime.now(),
                temporal = listOf(
                    PeriodOfTimeDBO(
                        LocalDate.of(2020, 10, 10),
                        LocalDate.of(2021, 4, 4)
                    )
                )
            )
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            datasetService.updateDatasetDBO(
                "catId",
                "dsId",
                listOf(JsonPatchOperation(OpEnum.REPLACE, "/temporal/0/startDate", "2020-10-10"))
            )

            argumentCaptor<List<DatasetDBO>>().apply {
                verify(datasetRepository, times(1)).saveAll(capture())
                assertTrue(firstValue.size == 1)
                assertEquals(
                    expected.copy(lastModified = firstValue.first().lastModified),
                    firstValue.first()
                )
            }
        }

        @Test
        fun `update dataset with copy operation`() {
            val ds = DatasetDBO("dsId", "catId", uri = "uri", title = LocalizedStrings(nb =  "tittel"), lastModified = LocalDateTime.now())
            val expected = DatasetDBO("dsId", "catId", uri = "uri", lastModified = null, title = LocalizedStrings(nb = "tittel", nn = "tittel"))
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            datasetService.updateDatasetDBO(
                "catId",
                "dsId",
                listOf(JsonPatchOperation(OpEnum.COPY, "/title/nn", null, "/title/nb"))
            )

            argumentCaptor<List<DatasetDBO>>().apply {
                verify(datasetRepository, times(1)).saveAll(capture())
                assertTrue(firstValue.size == 1)
                assertEquals(
                    expected.copy(lastModified = firstValue.first().lastModified),
                    firstValue.first()
                )
            }
        }

        @Test
        fun `update dataset with move operation`() {
            val ds = DatasetDBO(
                "dsId",
                "catId",
                uri = "uri",
                title = LocalizedStrings(nb = "beskrivelse"),
                description = null,
                lastModified = LocalDateTime.now()
            )
            val expected = DatasetDBO("dsId", "catId", title = null, uri = "uri", lastModified = null, description = LocalizedStrings(nb = "beskrivelse"))
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            datasetService.updateDatasetDBO(
                "catId",
                "dsId",
                listOf(JsonPatchOperation(OpEnum.MOVE, "/description", null, "/title"))
            )

            argumentCaptor<List<DatasetDBO>>().apply {
                verify(datasetRepository, times(1)).saveAll(capture())
                assertTrue(firstValue.size == 1)
                assertEquals(
                    expected.copy(lastModified = firstValue.first().lastModified),
                    firstValue.first()
                )
            }
        }

        @Test
        fun `update dataset with remove operation`() {
            val ds = DatasetDBO("dsId", "catId", type = "test", uri = "uri", lastModified = LocalDateTime.now())
            val expected = DatasetDBO("dsId", "catId", uri = "uri", lastModified = null)
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            datasetService.updateDatasetDBO("catId", "dsId", listOf(JsonPatchOperation(OpEnum.REMOVE, "/type")))

        argumentCaptor<List<DatasetDBO>>().apply {
            verify(datasetRepository, times(1)).saveAll(capture())
            assertTrue(firstValue.size == 1)
            assertEquals(
                expected.copy(lastModified = firstValue.first().lastModified),
                firstValue.first()
            )
        }
    }

        @Test
        fun `update of specialized type is ignored`() {
            val ds = DatasetDBO("dsId", "catId", uri = "uri", lastModified = LocalDateTime.now())
            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            assertThrows<ResponseStatusException> {
                datasetService.updateDatasetDBO(
                    "catId",
                    "dsId",
                    listOf(JsonPatchOperation(OpEnum.ADD, "/specializedType", "SERIES"))
                )
            }
            argumentCaptor<List<DatasetDBO>>().apply {
                verify(datasetRepository, times(0)).saveAll(capture())
            }
        }
    }

    @Nested
    internal inner class TriggerHarvest {
        @Test
        fun `triggers harvest on update to published dataset`() {
            val ds = DatasetDBO(
                "dsId",
                "catId",
                uri = "http://uri",
                published = true,
                approved = true,
                lastModified = LocalDateTime.now()
            )

            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(datasetRepository.save(any())).thenReturn(ds)

            datasetService.updateDatasetDBO("catId", "dsId", emptyList())

            verify(publishingService, times(1)).triggerHarvest("catId")
        }

        @Test
        fun `does not trigger harvest on update to draft dataset`() {
            val ds = DatasetDBO(
                "dsId",
                "catId",
                uri = "http://uri",
                published = false,
                approved = false,
                lastModified = LocalDateTime.now()
            )

            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(datasetRepository.save(any())).thenReturn(ds)

            datasetService.updateDatasetDBO("catId", "dsId", listOf(JsonPatchOperation(OpEnum.ADD, "/source", "hei")))

            verify(publishingService, times(0)).triggerHarvest("catId")
        }

        @Test
        fun `adds datasource on first published dataset in catalog`() {
            val ds = DatasetDBO(
                "dsId",
                "catId",
                uri = "http://uri",
                published = false,
                approved = true,
                lastModified = LocalDateTime.now()
            )

            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(datasetRepository.save(any())).thenReturn(ds)
            whenever(applicationProperties.datasetCatalogUriHost).thenReturn("http://mycatalog")

            datasetService.createDataset("catId", DatasetToCreate(approved = true))
            datasetService.updateDatasetDBO(
                "catId",
                "dsId",
                listOf(JsonPatchOperation(OpEnum.REPLACE, "/published", true))
            )

            verify(publishingService, times(1)).sendNewDataSourceMessage("catId", "http://mycatalog/catId")
        }

        @Test
        fun `does not add datasource on already added catalog`() {
            val ds0 = DatasetDBO(
                "dsId0",
                "catId",
                uri = "http://uri",
                published = false,
                approved = true,
                lastModified = LocalDateTime.now()
            )
            val ds1 = DatasetDBO(
                "dsId1",
                "catId",
                uri = "http://uri",
                published = false,
                approved = true,
                lastModified = LocalDateTime.now()
            )

            whenever(datasetRepository.findById("dsId1")).thenReturn(Optional.of(ds1))
            whenever(datasetRepository.save(any())).thenReturn(ds1)
            whenever(datasetRepository.findByCatalogId("catId")).thenReturn(listOf(ds0))

            datasetService.createDataset("catId", DatasetToCreate(approved = true))

            verify(publishingService, times(0)).sendNewDataSourceMessage(any(), any())
        }

        @Test
        fun `triggers harvest and adds datasource on first published dataset in catalog`() {
            val ds = DatasetDBO(
                "dsId",
                "catId",
                uri = "http://uri",
                published = true,
                approved = true,
                lastModified = LocalDateTime.now()
            )

            whenever(datasetRepository.findById("dsId")).thenReturn(Optional.of(ds))
            whenever(datasetRepository.save(any())).thenReturn(ds)
            whenever(applicationProperties.datasetCatalogUriHost).thenReturn("http://mycatalog")

            datasetService.updateDatasetDBO("catId", "dsId", emptyList())

            verify(publishingService, times(1)).triggerHarvest("catId")
            verify(publishingService, times(1)).sendNewDataSourceMessage("catId", "http://mycatalog/catId")
        }
    }

    @Nested
    internal inner class Resolve {

        @Test
        fun `Resolves references`() {
            val dataset = TEST_DATASET_1
            val referencedDataset = DatasetDBO(
                "1",
                "987654321",
                uri = "http://uri.no",
                originalUri = "http://originaluri/resolved",
                lastModified = LocalDateTime.now()
            )

            val resolved = dataset.references?.map { it.copy(source = "http://originaluri/resolved") }

            whenever(applicationProperties.catalogUriHost).thenReturn("http://mycatalog")
            whenever(datasetRepository.findById("1")).thenReturn(Optional.of(referencedDataset))

            assertEquals(resolved, datasetService.resolveDatasetReferences(dataset))
        }
    }
}
