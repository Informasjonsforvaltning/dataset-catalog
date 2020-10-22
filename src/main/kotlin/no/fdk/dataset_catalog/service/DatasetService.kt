package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.repository.DatasetRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.lang.Exception

@Service
class DatasetService (private val datasetRepository : DatasetRepository) {

    fun getByID (catalogId: String, id: String) : Dataset? {
        val dataset = datasetRepository.findByIdOrNull(id)
        return if (dataset != null && dataset.catalogId == catalogId) {
            dataset
        } else {
            null
        }
    }

    fun create (catalogId: String, dataset: Dataset) {
        if (catalogId == dataset.catalogId) {
            datasetRepository.save(dataset)
        } else {
            throw Exception("Catalog ID mismatch")
        }
    }

    fun count() {
        datasetRepository.count()
    }
}