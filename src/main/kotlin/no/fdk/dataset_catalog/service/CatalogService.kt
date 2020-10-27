package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.model.Catalog
import no.fdk.dataset_catalog.repository.CatalogRepository
import org.springframework.data.repository.findByIdOrNull

class CatalogService (val catalogRepository: CatalogRepository) {

    fun getAll(): List<Catalog> = catalogRepository.findAll()

    fun getByID(id: String): Catalog? = catalogRepository.findByIdOrNull(id)

    fun create(catalog: Catalog) {
        catalogRepository.save(catalog)
    }

    fun delete(catalogId: String) {
        getByID(catalogId)
            ?.let { catalogRepository.delete(it) }?: throw Exception()
    }
}