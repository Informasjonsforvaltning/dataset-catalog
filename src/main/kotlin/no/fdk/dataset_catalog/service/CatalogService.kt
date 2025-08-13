package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.model.CatalogCount
import no.fdk.dataset_catalog.repository.DatasetOperations
import org.springframework.stereotype.Service

@Service
class CatalogService(
    private val datasetOperations: DatasetOperations,
) {

    fun getAll(): List<CatalogCount> =
        datasetOperations.datasetCountForCatalogs(
            datasetOperations.getAllCatalogIds()
        )

    fun getByIDs(permittedOrgs: List<String>): List<CatalogCount> =
        datasetOperations.datasetCountForCatalogs(permittedOrgs)

    fun getByID(id: String): CatalogCount? =
        datasetOperations.datasetCountForCatalogs(listOf(id))
            .firstOrNull()

}
