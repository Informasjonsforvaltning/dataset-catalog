package no.fdk.dataset_catalog.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.json.Json
import jakarta.json.JsonException
import no.fdk.dataset_catalog.configuration.ApplicationProperties
import no.fdk.dataset_catalog.extensions.datasetToDBO
import no.fdk.dataset_catalog.extensions.toDataset
import no.fdk.dataset_catalog.model.*
import no.fdk.dataset_catalog.repository.DatasetRepository
import no.fdk.dataset_catalog.utils.isValidURI
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
    private val publishingService: PublishingService,
    private val applicationProperties: ApplicationProperties,
    private val mapper: ObjectMapper,
) {
    private var datasetUriPattern: Regex? = null

    fun getDatasetUriPattern(): Regex {
        if (datasetUriPattern == null) {
            datasetUriPattern = "^${applicationProperties.catalogUriHost}/\\d+/datasets/".toRegex()
        }
        return datasetUriPattern as Regex
    }

    fun getAll(catalogId: String, specializedTypeString: String? = null): List<Dataset> {
        val specializedType = specializedTypeFromString(specializedTypeString)
        val datasetList = if (specializedType == null) datasetRepository.findByCatalogId(catalogId)
        else datasetRepository.findByCatalogIdAndSpecializedType(catalogId, specializedType)

        return datasetList.map { dbo ->
            dbo.toDataset()
                .addOldAccessUrisToNewField()
                .addOldThemesToNewFields()
        }
    }

    fun getAllDatasets(catalogId: String, specializedTypeString: String? = null): List<DatasetDBO> {
        val specializedType = specializedTypeFromString(specializedTypeString)
        return if (specializedType == null) {
            datasetRepository.findByCatalogId(catalogId).toList()
        } else {
            datasetRepository.findByCatalogIdAndSpecializedType(catalogId, specializedType).toList()
        }
    }

    // Temporary function, remove when refactoring accessService in distribution
    private fun Distribution.addOldAccessUrisToNewField(): Distribution {
        val updatedAccessServiceUris: MutableSet<String> =
            accessServiceUris?.toMutableSet()
                ?: mutableSetOf()
        accessService?.mapNotNull { it.uri }
            ?.filter { it.isValidURI() }
            ?.forEach { updatedAccessServiceUris.add(it) }
        return if (updatedAccessServiceUris.isEmpty()) this
        else copy(accessServiceUris = updatedAccessServiceUris)
    }

    private fun Dataset.addOldAccessUrisToNewField(): Dataset {
        return copy(
            distribution = distribution?.map {
                it.addOldAccessUrisToNewField()
            }
        )
    }

    // Temporary function, remove when refactoring themes
    private fun Dataset.addOldThemesToNewFields(): Dataset {
        val validUris = theme
            ?.mapNotNull { it.uri }
            ?.filter { it.isValidURI() }
            ?.toList()
            ?: emptyList()

        val euDataThemes = validUris
            .filter { it.startsWith("http://publications.europa.eu/resource/authority/data-theme") }
            .toSet() + (euDataTheme ?: emptySet())

        val losThemes = validUris
            .filter { it.startsWith("https://psi.norge.no/los") }
            .toSet() + (losTheme ?: emptySet())

        return copy(
            euDataTheme = euDataThemes.ifEmpty { null },
            losTheme = losThemes.ifEmpty { null }
        )
    }

    fun getByID(catalogId: String, id: String): Dataset? {
        val dataset = datasetRepository.findByIdOrNull(id)
            ?.toDataset()
            ?.addOldAccessUrisToNewField()?.addOldThemesToNewFields()
        return if (dataset?.catalogId != catalogId) null else dataset
    }

    fun getDatasetByID(catalogId: String, id: String): DatasetDBO? {
        val dataset = datasetRepository.findByIdOrNull(id)
        return if (dataset?.catalogId != catalogId) null else dataset
    }


    private fun getByID(id: String): Dataset? {
        return datasetRepository.findByIdOrNull(id)
            ?.toDataset()
            ?.addOldAccessUrisToNewField()?.addOldThemesToNewFields()
    }

    fun getListByIDs(catalogId: String, ids: List<String>) =
        datasetRepository.findAllById(ids).filter { it.catalogId == catalogId }
            .map { it.toDataset().addOldAccessUrisToNewField().addOldThemesToNewFields() }

    fun getDatasetListByIDs(catalogId: String, ids: List<String>) =
        datasetRepository.findAllById(ids).filter { it.catalogId == catalogId }

    fun create(catalogId: String, dataset: Dataset): Dataset? {
        val datasetId = dataset.id ?: UUID.randomUUID().toString()

        dataset.copy(
            id = datasetId,
            catalogId = catalogId,
            lastModified = LocalDateTime.now(),
            uri = "${applicationProperties.catalogUriHost}/$catalogId/datasets/$datasetId",
            registrationStatus = dataset.registrationStatus ?: REGISTRATION_STATUS.DRAFT,
        )
            .allAffectedSeriesDatasets(null)
            .let { persistAndHarvest(it, catalogId) }

        return getByID(catalogId, datasetId)
    }

    fun updateDataset(catalogId: String, id: String, operations: List<JsonPatchOperation>): Dataset? {
        val dataset = getByID(catalogId, id)

        dataset?.update(operations)
            ?.copy(
                id = id,
                catalogId = catalogId,
                specializedType = dataset.specializedType,
                lastModified = LocalDateTime.now()
            )
            ?.allAffectedSeriesDatasets(dataset)
            ?.let { persistAndHarvest(it, catalogId) }

        return getByID(catalogId, id)
    }

    fun delete(catalogId: String, id: String) {
        getByID(catalogId, id)
            ?.also { datasetRepository.delete(it.datasetToDBO()) }
            ?.removeDeletedDatasetFromSeriesFields()
            ?: throw Exception()
    }

    private fun Dataset.removeDeletedDatasetFromSeriesFields() {
        if (id != null) {
            inSeries?.let { datasetRepository.findByIdOrNull(it) }
                ?.let { it.copy(seriesDatasetOrder = it.seriesDatasetOrder?.minus(id)) }
                ?.let { datasetRepository.save(it) }

            seriesDatasetOrder?.let { datasetRepository.findAllById(it.keys) }
                ?.map { it.copy(inSeries = null) }
                ?.let { datasetRepository.saveAll(it) }
        }
    }

    fun resolveReferences(ds: Dataset): List<Reference>? =
        ds.references?.map {
            val originalUri: String? = if (isDatasetReference(it)) {
                it.source?.uri?.let { uri ->
                    getByID(uri.substring(uri.lastIndexOf("/") + 1))
                        ?.originalUri
                }
            } else null

            if (originalUri != null) {
                Reference(
                    referenceType = it.referenceType,
                    source = SkosConcept(originalUri, it.source?.prefLabel, it.source?.extraType)
                )
            } else {
                it
            }
        }

    fun resolveDatasetReferences(ds: DatasetDBO): List<ReferenceDBO>? =
        ds.references?.map { ref ->
            val originalUri: String? = if (ref?.source?.let { getDatasetUriPattern().containsMatchIn(it) } == true) {
                ref.source.substringAfterLast("/").let { id ->
                    getByID(id)?.originalUri
                }
            } else {
                null
            }

            if (originalUri != null) {
                ReferenceDBO(
                    referenceType = ref.referenceType,
                    source = originalUri
                )
            } else {
                ref
            }
        }

    private fun Dataset.allAffectedSeriesDatasets(dbDataset: Dataset?): List<Dataset> =
        if (id == null) listOf(this)
        else {
            val addedInSeries = inSeries
                ?.takeIf { it != dbDataset?.inSeries }
                ?.let { datasetRepository.findByIdOrNull(it)?.toDataset() }
                ?.let {
                    val updatedSeriesOrder = if (it.seriesDatasetOrder.isNullOrEmpty()) {
                        mapOf(Pair(id, 0))
                    } else {
                        it.seriesDatasetOrder.plus(Pair(id, it.seriesDatasetOrder.values.max() + 1))
                    }
                    it.copy(seriesDatasetOrder = updatedSeriesOrder)
                }
                ?.let { listOf(it) } ?: emptyList()

            val removedInSeries = dbDataset?.inSeries
                ?.takeIf { it != inSeries }
                ?.let { datasetRepository.findByIdOrNull(it) }
                ?.let { it.toDataset().copy(seriesDatasetOrder = it.seriesDatasetOrder?.minus(id)) }
                ?.let { listOf(it) } ?: emptyList()

            val addedToOrder = if (specializedType == SpecializedType.SERIES) {
                seriesDatasetOrder?.keys
                    ?.filter { it !in (dbDataset?.seriesDatasetOrder?.keys ?: emptyList()) }
                    ?.let { datasetRepository.findAllById(it) }
                    ?.map { it.toDataset().copy(inSeries = id) }
                    ?: emptyList()
            } else emptyList()

            val removedFromOrder = if (specializedType == SpecializedType.SERIES) {
                dbDataset?.seriesDatasetOrder?.keys
                    ?.filter { it !in (seriesDatasetOrder?.keys ?: emptyList()) }
                    ?.let { datasetRepository.findAllById(it) }
                    ?.map { it.toDataset().copy(inSeries = null) }
                    ?: emptyList()
            } else emptyList()

            listOf(listOf(this), addedInSeries, removedInSeries, addedToOrder, removedFromOrder).flatten()
        }

    private fun isDatasetReference(ref: Reference?): Boolean =
        ref?.source?.uri?.let { getDatasetUriPattern().containsMatchIn(it) } ?: false

    private fun persistAndHarvest(datasets: List<Dataset>, catalogId: String) {
        val isFirstPublished = isFirstPublishedDatasetForCatalog(datasets, catalogId)

        val datasetDBOs = datasets.map { it.datasetToDBO() }
        datasetRepository
            .saveAll(datasetDBOs)
            .also {
                if (isFirstPublished) addDataSource(catalogId)
                triggerHarvest(datasets, catalogId)
            }
    }

    fun addDataSource(catalogId: String) {
        publishingService.sendNewDataSourceMessage(
            catalogId,
            "${applicationProperties.datasetCatalogUriHost}/$catalogId"
        )
    }

    private fun triggerHarvest(datasets: List<Dataset>, catalogId: String) {
        if (datasets.any { it.registrationStatus == REGISTRATION_STATUS.PUBLISH }) {
            publishingService.triggerHarvest(catalogId)
        }
    }

    private fun isFirstPublishedDatasetForCatalog(datasets: List<Dataset>, catalogId: String): Boolean =
        when {
            datasets.none { it.registrationStatus == REGISTRATION_STATUS.PUBLISH } -> false
            getAll(catalogId).any { it.registrationStatus == REGISTRATION_STATUS.PUBLISH } -> false
            else -> true
        }

    private fun Dataset.update(operations: List<JsonPatchOperation>): Dataset {
        validateOperations(operations)
        return try {
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

    fun validateOperations(operations: List<JsonPatchOperation>) {
        val invalidPaths = listOf(
            "/id",
            "/catalogId",
            "/specializedType",
            "/uri",
            "/originalUri"
        )
        if (operations.any { it.path in invalidPaths }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Patch of paths $invalidPaths is not permitted")
        }
    }

    private fun specializedTypeFromString(string: String?): SpecializedType? =
        try {
            string?.uppercase()?.let { SpecializedType.valueOf(it) }
        } catch (e: java.lang.IllegalArgumentException) {
            null
        }

}
