package no.fdk.dataset_catalog.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.json.Json
import jakarta.json.JsonException
import no.fdk.dataset_catalog.configuration.ApplicationProperties
import no.fdk.dataset_catalog.extensions.addCreateValues
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

    fun getAllDatasets(catalogId: String, specializedTypeString: String? = null): List<DatasetDBO> {
        val specializedType = specializedTypeFromString(specializedTypeString)
        return if (specializedType == null) {
            datasetRepository.findByCatalogId(catalogId).toList()
        } else {
            datasetRepository.findByCatalogIdAndSpecializedType(catalogId, specializedType).toList()
        }
    }

    fun getDatasetByID(catalogId: String, id: String): DatasetDBO? {
        val dataset = datasetRepository.findByIdOrNull(id)
        return if (dataset?.catalogId != catalogId) null else dataset
    }

    fun getDatasetListByIDs(catalogId: String, ids: List<String>) =
        datasetRepository.findAllById(ids).filter { it.catalogId == catalogId }

    fun createDataset(catalogId: String, values: DatasetToCreate): String {
        val datasetId = UUID.randomUUID().toString()

        val newDataset = DatasetDBO(
            id = datasetId,
            catalogId = catalogId,
            lastModified = LocalDateTime.now(),
            uri = "${applicationProperties.catalogUriHost}/$catalogId/datasets/$datasetId",
            published = false,
            approved = false
        )

        newDataset.addCreateValues(values)
            .allAffectedSeriesDatasets(null)
            .let { persistAndHarvestDatasets(it, catalogId) }

        return datasetId
    }

    fun updateDatasetDBO(catalogId: String, id: String, operations: List<JsonPatchOperation>): DatasetDBO? {
        val dataset = getDatasetByID(catalogId, id)

        dataset?.update(operations)
            ?.copy(
                id = id,
                catalogId = catalogId,
                specializedType = dataset.specializedType,
                lastModified = LocalDateTime.now()
            )
            ?.allAffectedSeriesDatasets(dataset)
            ?.let { persistAndHarvestDatasets(it, catalogId) }

        return getDatasetByID(catalogId, id)
    }

    fun delete(catalogId: String, id: String) {
        getDatasetByID(catalogId, id)
            ?.also { datasetRepository.delete(it) }
            ?.removeDeletedDatasetFromSeriesFields()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    private fun DatasetDBO.removeDeletedDatasetFromSeriesFields() {
        inSeries?.let { datasetRepository.findByIdOrNull(it) }
            ?.let { it.copy(seriesDatasetOrder = it.seriesDatasetOrder?.minus(id)) }
            ?.let { datasetRepository.save(it) }

        seriesDatasetOrder?.let { datasetRepository.findAllById(it.keys) }
            ?.map { it.copy(inSeries = null) }
            ?.let { datasetRepository.saveAll(it) }
    }

    fun resolveDatasetReferences(ds: DatasetDBO): List<ReferenceDBO>? =
        ds.references?.map { ref ->
            val originalUri: String? = if (ref.source?.let { getDatasetUriPattern().containsMatchIn(it) } == true) {
                ref.source.substringAfterLast("/").let { id ->
                    datasetRepository.findByIdOrNull(id)?.originalUri
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

    private fun DatasetDBO.allAffectedSeriesDatasets(dbDataset: DatasetDBO?): List<DatasetDBO> =
        run {
            val addedInSeries = inSeries
                ?.takeIf { it != dbDataset?.inSeries }
                ?.let { datasetRepository.findByIdOrNull(it) }
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
                ?.let { it.copy(seriesDatasetOrder = it.seriesDatasetOrder?.minus(id)) }
                ?.let { listOf(it) } ?: emptyList()

            val addedToOrder = if (specializedType == SpecializedType.SERIES) {
                seriesDatasetOrder?.keys
                    ?.filter { it !in (dbDataset?.seriesDatasetOrder?.keys ?: emptyList()) }
                    ?.let { datasetRepository.findAllById(it) }
                    ?.map { it.copy(inSeries = id) }
                    ?: emptyList()
            } else emptyList()

            val removedFromOrder = if (specializedType == SpecializedType.SERIES) {
                dbDataset?.seriesDatasetOrder?.keys
                    ?.filter { it !in (seriesDatasetOrder?.keys ?: emptyList()) }
                    ?.let { datasetRepository.findAllById(it) }
                    ?.map { it.copy(inSeries = null) }
                    ?: emptyList()
            } else emptyList()

            listOf(listOf(this), addedInSeries, removedInSeries, addedToOrder, removedFromOrder).flatten()
        }

    private fun persistAndHarvestDatasets(datasets: List<DatasetDBO>, catalogId: String) {
        val isFirstPublished = isFirstPublishedDatasetForCatalog(datasets, catalogId)

        datasetRepository
            .saveAll(datasets)
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

    private fun triggerHarvest(datasets: List<DatasetDBO>, catalogId: String) {
        if (datasets.any { it.published == true }) {
            publishingService.triggerHarvest(catalogId)
        }
    }

    private fun isFirstPublishedDatasetForCatalog(datasets: List<DatasetDBO>, catalogId: String): Boolean =
        when {
            datasets.none { it.published == true } -> false
            getAllDatasets(catalogId).any { it.published == true } -> false
            else -> true
        }

    private fun DatasetDBO.update(operations: List<JsonPatchOperation>): DatasetDBO {
        validateOperations(operations)
        return try {
            patchDatasetDBO(this, operations)
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

    private fun patchDatasetDBO(dataset: DatasetDBO, operations: List<JsonPatchOperation>): DatasetDBO {
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
