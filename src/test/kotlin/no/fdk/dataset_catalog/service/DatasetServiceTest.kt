package no.fdk.dataset_catalog.service

import com.nhaarman.mockitokotlin2.*
import no.fdk.dataset_catalog.extensions.updateSubjects
import no.fdk.dataset_catalog.model.Concept
import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.model.Subject
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
    private val conceptCatClientService: ConceptCatClientService = mock()
    private val datasetService = DatasetService(datasetRepository, conceptCatClientService)

    @Nested
    internal inner class Create {
        @Test
        fun `persists new dataset`() {
            val ds = Dataset("dsId", "catID")
            datasetService.create("catId", ds)
            verify(datasetRepository, times(1)).save(any())
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
            val actual = datasetService.updateDataset("catId","dsId", expected)

            assertEquals(expected, actual)
        }
    }

    @Nested
    internal inner class UpdateConcepts {
        @Test
        fun `updates concepts`() {
            val ds = Dataset("dsId", "catId", concepts = listOf(Concept("1")))
            val expected = Dataset("dsId", "catId", concepts = listOf(Concept("1", uri="uri")))
            whenever(conceptCatClientService.getByIds(listOf("1"))).thenReturn(listOf(Concept("1", uri="uri")))
            val actual = with(datasetService) {ds.updateConcepts()}
            assertEquals(expected, actual)
        }
    }

    @Nested
    internal inner class UpdateSubjects {
        @Test
        fun `updates subjects`() {
            val ds = Dataset("dsId", "catId", concepts = listOf(Concept("1", "uri")))
            val expected = ds.copy(subjects = listOf(Subject(id = "1", uri = "uri")))
            val actual = ds.updateSubjects()
            assertEquals(expected, actual)
        }
    }

}