package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.extensions.update
import no.fdk.dataset_catalog.extensions.updateSubjects
import no.fdk.dataset_catalog.model.*
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
    private val publishingService: PublishingService
) {

    fun getAll(catalogId: String): List<Dataset> =
        datasetRepository.findByCatalogId(catalogId) as List<Dataset>

    fun getByID(catalogId: String, id: String): Dataset? {
        val dataset = datasetRepository.findByIdOrNull(id)
        return if (dataset != null && dataset.catalogId == catalogId) {
            dataset
        } else {
            null
        }
    }

    fun create(catalogId: String, dataset: Dataset): Dataset? {
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
            .let {
                persistAndHarvest(it, catalog)
            }
    }

    fun count() {
        datasetRepository.count()
    }

    fun updateDataset(catalogId: String, id: String, patch: Dataset): Dataset? {
        val dataset = getByID(catalogId, id)

        return dataset
            ?.update(patch)
            ?.updateConcepts()
            ?.updateSubjects()
            ?.let {
                persistAndHarvest(it, catalogService.getByID(catalogId))
            }
    }

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

    private fun persistAndHarvest(dataset: Dataset?, catalog: Catalog?): Dataset? =
        if (dataset != null && catalog != null) {
            datasetRepository
                .save(dataset)
                .also {
                addDataSource(dataset, catalog)
                triggerHarvest(dataset, catalog)
            }
        } else null


    private fun triggerHarvest(dataset: Dataset, catalog: Catalog) {
        if (dataset.registrationStatus == REGISTRATION_STATUS.PUBLISH) publishingService.triggerHarvest(dataset.id, catalog.id, catalog.publisher?.id)
    }

    private fun addDataSource(dataset: Dataset, catalog: Catalog) {
        if (dataset.registrationStatus == REGISTRATION_STATUS.PUBLISH && catalog.hasPublishedDataSource == false) catalogService.addDataSource(catalog)
    }
}
