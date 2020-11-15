package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.extensions.update
import no.fdk.dataset_catalog.extensions.updateSubjects
import no.fdk.dataset_catalog.model.Concept
import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.model.Publisher
import no.fdk.dataset_catalog.model.REGISTRATION_STATUS
import no.fdk.dataset_catalog.repository.DatasetRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*


@Service
class DatasetService(
    private val datasetRepository: DatasetRepository,
    private val catalogService: CatalogService,
    private val organizationService: OrganizationService,
    private val conceptService: ConceptService,
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

    fun create(catalogId: String, dataset: Dataset): Dataset {
        val catalog = catalogService.getByID(catalogId) ?: throw Exception("Catalog not found")
        val datasetId = dataset.id ?: UUID.randomUUID().toString()

        if (dataset.publisher != null) {
            validateDatasetPublisher(catalog.publisher, dataset.publisher)
        }

        return dataset
            .copy(
                id = datasetId,
                catalogId = catalogId,
                lastModified = LocalDateTime.now(),
                uri = "http://brreg.no/catalogs/$catalogId/datasets/$datasetId",
                publisher = dataset.publisher ?: catalog.publisher,
                registrationStatus = dataset.registrationStatus ?: REGISTRATION_STATUS.DRAFT)
            .updateConcepts()
            .updateSubjects()
            .let { datasetRepository.save(it) }
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

    fun delete(catalogId: String, id: String) {
        getByID(catalogId, id)
            ?.let { datasetRepository.delete(it) }?: throw Exception()
    }

    fun Dataset.updateConcepts(): Dataset =
        if (concepts != null && concepts.isNotEmpty()) {
            copy(concepts = getConceptsByID(concepts))
        } else this

    private fun validateDatasetPublisher(catalogPublisher: Publisher?, datasetPublisher: Publisher) {
        if (datasetPublisher.id != null && catalogPublisher?.id !=null &&
            datasetPublisher.id != catalogPublisher.id &&
            !organizationService.hasDelegationPermission(catalogPublisher.id)) {
            throw Exception(
                "Organization with ID ${catalogPublisher.id} has no delegation permission to create datasets on behalf of other organizations"
            )
        }
    }

    private fun getConceptsByID(patchConcepts: Collection<Concept>): List<Concept> =
        conceptService.getConcepts(patchConcepts.mapNotNull { it.id })

}
