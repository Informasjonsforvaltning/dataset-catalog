package no.fdk.dataset_catalog.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.repository.DatasetRepository
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertNull

@Tag("unit")
class DatasetServiceTest {
    private val datasetRepository: DatasetRepository = mock()
    private val datasetService = DatasetService(datasetRepository)

    @Nested
    internal inner class GetById {

        @Test
        fun findByIdUsesCorrectMethod() {
            whenever(datasetRepository.findById("non-existing-id")).thenReturn(Optional.empty<Dataset>())
            val dataset = datasetService.getByID("non-existing-catalog-id","non-existing-id")
            assertNull(dataset)
        }

    }
}