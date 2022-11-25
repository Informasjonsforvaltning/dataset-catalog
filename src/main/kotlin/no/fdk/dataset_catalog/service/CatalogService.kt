package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.configuration.ApplicationProperties
import no.fdk.dataset_catalog.extensions.*
import no.fdk.dataset_catalog.model.Catalog
import no.fdk.dataset_catalog.model.CatalogDTO
import no.fdk.dataset_catalog.model.Publisher
import no.fdk.dataset_catalog.repository.CatalogRepository
import no.fdk.dataset_catalog.repository.DatasetRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CatalogService(private val catalogRepository: CatalogRepository,
                     private val datasetRepository: DatasetRepository,
                     private val organizationService: OrganizationService,
                     private val publishingService: PublishingService,
                     private val applicationProperties: ApplicationProperties) {

    fun getAll(): List<Catalog> = catalogRepository.findAll()

    fun getAllAsDTO(): CatalogDTO = getAll()
        .map { it.addDatasetCount()}
        .toDTO()

    fun getByID(id: String): Catalog? = catalogRepository.findByIdOrNull(id)

    fun create(catalog: Catalog) {
        catalog.updatePublisher()
            .updateUriIfNeeded(applicationProperties.catalogUriHost)
            .let { catalogRepository.save(it) }
    }

    fun createCatalogsIfNeeded(adminableOrgs: Set<String>) =
        adminableOrgs.forEach{
            if (getByID(it) == null){
                try {
                    val organizationName: String? = organizationService.getOrganization(it)?.name
                    if (organizationName != null) {
                        create(Catalog(id=it, title = mapOf( Pair("nb", "Datakatalog for $organizationName") )))
                    } else {
                        create(Catalog(id=it))
                    }
                } catch (e: Exception) {
                }
            }
        }

    fun delete(catalogId: String) {
        getByID(catalogId)
            ?.let { catalogRepository.delete(it) }?: throw Exception()
    }

    fun update(catalogId: String, catalog: Catalog): Catalog? =
        getByID(catalogId)
            ?.verifyId(catalog)
            ?.updatePublisherIfNeeded()
            ?.updateUriIfNeeded(applicationProperties.catalogUriHost)
            ?.update(catalog)
            ?.let { catalogRepository.save(it) }


    fun Catalog.updatePublisherIfNeeded(): Catalog =
        if (publisher == null) {
            updatePublisher()
        } else this

    private fun Catalog.updatePublisher(): Catalog =
        if (id != null) {
            copy(
                publisher = Publisher(
                    id = id,
                    name = organizationService.getOrganization(id)?.name,
                    uri = "${applicationProperties.organizationCatalogHost}/organizations/$id"
                )
            )
        } else this

    fun getByIDs(permittedOrgs: Set<String>): List<Catalog> = catalogRepository.findAllById(permittedOrgs).toList()

    fun getByIDsAsDTO(permittedOrgs: Set<String>): CatalogDTO = getByIDs(permittedOrgs)
        .map { it.addDatasetCount() }
        .toDTO()

    fun addDataSource(catalog: Catalog) {
        val success = publishingService.sendNewDataSourceMessage(
            catalog.id,
            "${applicationProperties.datasetCatalogUriHost}/${catalog.id}"
        )
        if (success) {
            catalogRepository.save(
                catalog.copy(hasPublishedDataSource = true)
            )
        }
    }

    private fun Catalog.addDatasetCount(): Catalog =
        copy(datasetCount = id?.let { datasetRepository.findByCatalogId(it) }?.size ?: 0)
}