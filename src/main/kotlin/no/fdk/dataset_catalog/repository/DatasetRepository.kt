package no.fdk.dataset_catalog.repository

import no.fdk.dataset_catalog.model.DatasetDBO
import no.fdk.dataset_catalog.model.SpecializedType
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

const val matchTitle =
    "{" +
        "'catalogId' : {\$in: ?0}," +
        "\$or : [{'title.nb' : { \$regex: /\\b?1/, \$options: 'i' }}, {'title.nn' : { \$regex: /\\b?1/, \$options: 'i' }}, {'title.en' : { \$regex: /\\b?1/, \$options: 'i' }}]" +
    "}"

const val matchDescription =
    "{" +
        "'catalogId' : {\$in: ?0}," +
        "\$or : [{'description.nb' : { \$regex: /\\b?1/, \$options: 'i' }}, {'description.nn' : { \$regex: /\\b?1/ , \$options: 'i'}}, {'description.en' : { \$regex: /\\b?1/, \$options: 'i' }}]" +
    "}"
@Repository
interface DatasetRepository : MongoRepository<DatasetDBO, String?> {
    fun findByCatalogId(catalogId: String) : Collection<DatasetDBO>
    fun findByCatalogIdAndSpecializedType(catalogId: String, specializedType: SpecializedType) : Collection<DatasetDBO>

    @Query(matchTitle)
    fun findByTitleContaining(
        @Param("id") catalogId: List<String>,
        @Param("query") query: String
    ) : Set<DatasetDBO>

    @Query(matchDescription)
    fun findByDescriptionContaining(
        @Param("id") catalogId: List<String>,
        @Param("query") query: String
    ) : Set<DatasetDBO>
}