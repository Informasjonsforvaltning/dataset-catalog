package no.fdk.dataset_catalog.repository

import no.fdk.dataset_catalog.model.CatalogCount
import no.fdk.dataset_catalog.model.DatasetDBO
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Service

@Service
class DatasetOperations(private val mongoOperations: MongoOperations) {

    fun datasetCountForCatalogs(catalogIds: List<String>): List<CatalogCount> {
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("catalogId").`in`(catalogIds)),
            Aggregation.group("catalogId")
                .count().`as`("datasetCount")
        )

        return mongoOperations.aggregate(aggregation, "datasets", CatalogCount::class.java).toList()
    }

    fun getAllCatalogIds(): List<String> =
        mongoOperations.query(DatasetDBO::class.java)
            .distinct("catalogId")
            .`as`(String::class.java)
            .all()

}
