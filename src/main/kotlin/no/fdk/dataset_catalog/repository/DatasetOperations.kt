package no.fdk.dataset_catalog.repository

import jakarta.persistence.EntityManager
import no.fdk.dataset_catalog.model.CatalogCount
import no.fdk.dataset_catalog.model.DatasetEntity
import org.springframework.stereotype.Service

@Service
class DatasetOperations(private val entityManager: EntityManager) {

    fun datasetCountForCatalogs(catalogIds: List<String>): List<CatalogCount> {
        if (catalogIds.isEmpty()) return emptyList()
        val query = entityManager.createQuery(
            """
            SELECT new no.fdk.dataset_catalog.model.CatalogCount(d.catalogId, COUNT(d))
            FROM DatasetEntity d
            WHERE d.catalogId IN :catalogIds
            GROUP BY d.catalogId
            """,
            CatalogCount::class.java
        )
        query.setParameter("catalogIds", catalogIds)
        return query.resultList
    }

    fun getAllCatalogIds(): List<String> {
        val query = entityManager.createQuery(
            "SELECT DISTINCT d.catalogId FROM DatasetEntity d",
            String::class.java
        )
        return query.resultList
    }

}
