package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.extensions.*
import no.fdk.dataset_catalog.model.Catalog
import no.fdk.dataset_catalog.model.Publisher
import no.fdk.dataset_catalog.repository.CatalogRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CatalogService(val catalogRepository: CatalogRepository,
                     val organizationService: OrganizationService) {
    @Value("\${application.openDataEnhet}")
    private val openDataEnhetsregisteret: String? = null

    fun getAll(): List<Catalog> = catalogRepository.findAll()

    fun getByID(id: String): Catalog? = catalogRepository.findByIdOrNull(id)

    fun create(catalog: Catalog) {
        catalog.updatePublisher()
            .updateUriIfNeeded()
            .let { catalogRepository.save(it) }
    }
    fun delete(catalogId: String) {
        getByID(catalogId)
            ?.let { catalogRepository.delete(it) }?: throw Exception()
    }

    fun update(catalogId: String, catalog: Catalog): Catalog? =
        getByID(catalogId)
            ?.verifyId(catalog)
            ?.updatePublisherIfNeeded()
            ?.updateUriIfNeeded()
            ?.update(catalog)
            ?.let { catalogRepository.save(it) }


    private fun Catalog.updatePublisherIfNeeded(): Catalog =
        if (publisher == null) {
            updatePublisher()
        } else this

    private fun Catalog.updatePublisher(): Catalog =
        if (id != null) {
            copy(
                publisher = Publisher(
                    id = id,
                    name = organizationService.getByOrgNr(id)?.name,
                    uri = openDataEnhetsregisteret + id
                )
            )
        } else this



}