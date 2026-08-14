package no.fdk.datasetcatalog.service

import no.fdk.datasetcatalog.model.CatalogCount
import no.fdk.datasetcatalog.repository.DatasetOperations
import org.springframework.stereotype.Service

@Service
class CatalogService(private val datasetOperations: DatasetOperations) {
    fun getAll(): List<CatalogCount> = datasetOperations.datasetCountForCatalogs(
        datasetOperations.getAllCatalogIds(),
    )

    fun getByIDs(permittedOrgs: List<String>): List<CatalogCount> = datasetOperations.datasetCountForCatalogs(permittedOrgs)

    fun getByID(id: String): CatalogCount? = datasetOperations
        .datasetCountForCatalogs(listOf(id))
        .firstOrNull()
}
