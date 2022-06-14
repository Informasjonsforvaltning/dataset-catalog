package no.fdk.dataset_catalog.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.json.Json
import jakarta.json.JsonException
import no.fdk.dataset_catalog.configuration.ApplicationProperties
import no.fdk.dataset_catalog.extensions.updateSubjects
import no.fdk.dataset_catalog.model.*
import no.fdk.dataset_catalog.repository.DatasetRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.io.StringReader
import java.time.LocalDateTime
import java.util.*

private val logger = LoggerFactory.getLogger(DatasetService::class.java)

@Service
class DatasetService(
    private val datasetRepository: DatasetRepository,
    private val catalogService: CatalogService,
    private val organizationService: OrganizationService,
    private val conceptService: ConceptService,
    private val publishingService: PublishingService,
    private val applicationProperties: ApplicationProperties,
    private val mapper: ObjectMapper
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
                uri = "${applicationProperties.catalogUriHost}/$catalogId/datasets/$datasetId",
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

    fun updateDataset(catalogId: String, id: String, operations: List<JsonPatchOperation>): Dataset? {
        val dataset = getByID(catalogId, id)

        return dataset
            ?.update(operations)
            ?.copy(id = id, catalogId = catalogId, lastModified = LocalDateTime.now())
            ?.updateConcepts()
            ?.updateSubjects()
            ?.let { persistAndHarvest(it, catalogService.getByID(catalogId)) }
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

    private fun Dataset.update(operations: List<JsonPatchOperation>): Dataset =
        try {
            patchDataset(this, operations)
        } catch (ex: Exception) {
            logger.error("PATCH failed for $id", ex)
            when (ex) {
                is JsonException -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message)
                is JsonProcessingException -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message)
                is IllegalArgumentException -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message)
                is java.lang.ClassCastException -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message)
                else -> throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.message)
            }
        }

    private fun patchDataset(dataset: Dataset, operations: List<JsonPatchOperation>): Dataset {
        if (operations.isNotEmpty()) {
            with(mapper) {
                val changes = Json.createReader(StringReader(writeValueAsString(operations))).readArray()
                val original = Json.createReader(StringReader(writeValueAsString(dataset))).readObject()

                return Json.createPatch(changes).apply(original)
                    .let { readValue(it.toString()) }
            }
        }
        return dataset
    }
}
