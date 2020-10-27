package no.fdk.dataset_catalog.repository

import no.fdk.dataset_catalog.model.Catalog
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CatalogRepository : MongoRepository<Catalog, String?>