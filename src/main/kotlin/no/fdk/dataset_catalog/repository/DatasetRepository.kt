package no.fdk.dataset_catalog.repository

import no.fdk.dataset_catalog.model.Dataset
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

const val matchTitle =
    "{" +
        "'catalogId' : {\$in: ?0}," +
        "\$or : [{'title.nb' : { \$regex: ?1 }}, {'title.nn' : { \$regex: ?1 }}, {'title.en' : { \$regex: ?1 }}]" +
    "}"

const val matchDescription =
    "{" +
        "'catalogId' : {\$in: ?0}," +
        "\$or : [{'description.nb' : { \$regex: ?1 }}, {'description.nn' : { \$regex: ?1 }}, {'description.en' : { \$regex: ?1 }}]" +
    "}"
@Repository
interface DatasetRepository : MongoRepository<Dataset, String?> {
    fun findByCatalogId(catalogId: String) : Collection<Dataset>

    @Query(matchTitle)
    fun findByTitleContaining(
        @Param("id") catalogId: List<String>,
        @Param("query") query: String
    ) : Set<Dataset>

    @Query(matchDescription)
    fun findByDescriptionContaining(
        @Param("id") catalogId: List<String>,
        @Param("query") query: String
    ) : Set<Dataset>
}