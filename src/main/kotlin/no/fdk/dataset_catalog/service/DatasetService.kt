package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.extensions.*
import no.fdk.dataset_catalog.model.*
import no.fdk.dataset_catalog.repository.DatasetRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*


@Service
class DatasetService (
    private val datasetRepository : DatasetRepository,
    private val conceptCatClientService: ConceptCatClientService,
) {

    fun getAll(catalogId: String): Collection<Dataset> =
        datasetRepository.findByCatalogId(catalogId)

    fun getByID(catalogId: String, id: String): Dataset? {
        val dataset = datasetRepository.findByIdOrNull(id)
        return if (dataset != null && dataset.catalogId == catalogId) {
            dataset
        } else {
            null
        }
    }

    fun create(catalogId: String, dataset: Dataset) {
        dataset
            .copy(
                id = UUID.randomUUID().toString(),
                catalogId = catalogId)
            .updateConcepts()
            .updateSubjects()
            .run { datasetRepository.save(this) }
    }

    fun count() {
        datasetRepository.count()
    }

    fun updateDataset(catalogId: String, id: String, patch: Dataset): Dataset? =
        getByID(catalogId, id)
            ?.update(patch)
            ?.updateConcepts()
            ?.updateSubjects()
            ?.let { datasetRepository.save(it) }


    fun Dataset.updateConcepts(): Dataset =
        if (concepts != null && concepts.isNotEmpty()) {
            copy(concepts = getConceptsByID(concepts))
        } else this

    private fun getConceptsByID(patchConcepts: Collection<Concept>): List<Concept> =
        conceptCatClientService.getByIds(patchConcepts.mapNotNull {it.id})

    fun delete(catalogId: String, id: String) {
        getByID(catalogId, id)
            ?.let { datasetRepository.delete(it) }?: throw Exception()
    }

}
