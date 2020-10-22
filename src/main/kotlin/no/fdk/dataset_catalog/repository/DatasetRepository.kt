package no.fdk.dataset_catalog.repository

import no.fdk.dataset_catalog.model.Dataset
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository


@Repository
interface DatasetRepository : MongoRepository<Dataset, String?>