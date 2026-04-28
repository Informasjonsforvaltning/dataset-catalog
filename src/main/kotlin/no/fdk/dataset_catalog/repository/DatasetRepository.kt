package no.fdk.dataset_catalog.repository

import no.fdk.dataset_catalog.model.DatasetEntity
import no.fdk.dataset_catalog.model.SpecializedType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DatasetRepository : JpaRepository<DatasetEntity, String> {
    fun findByCatalogId(catalogId: String): List<DatasetEntity>
    fun findByCatalogIdAndSpecializedType(catalogId: String, specializedType: SpecializedType): List<DatasetEntity>

    @Query(
        value = """
            SELECT * FROM datasets d
            WHERE d.catalog_id IN (:catalogIds)
            AND (
                d.data->'title'->>'nb' ILIKE '%' || :query || '%'
                OR d.data->'title'->>'nn' ILIKE '%' || :query || '%'
                OR d.data->'title'->>'en' ILIKE '%' || :query || '%'
            )
        """,
        nativeQuery = true
    )
    fun findByTitleContaining(
        @Param("catalogIds") catalogIds: List<String>,
        @Param("query") query: String
    ): List<DatasetEntity>

    @Query(
        value = """
            SELECT * FROM datasets d
            WHERE d.catalog_id IN (:catalogIds)
            AND (
                d.data->'description'->>'nb' ILIKE '%' || :query || '%'
                OR d.data->'description'->>'nn' ILIKE '%' || :query || '%'
                OR d.data->'description'->>'en' ILIKE '%' || :query || '%'
            )
        """,
        nativeQuery = true
    )
    fun findByDescriptionContaining(
        @Param("catalogIds") catalogIds: List<String>,
        @Param("query") query: String
    ): List<DatasetEntity>
}
