package no.fdk.dataset_catalog.model

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "datasets")
data class DatasetEntity(
    @Id
    @Column(name = "id")
    val id: String = "",

    @Column(name = "catalog_id", nullable = false)
    val catalogId: String = "",

    @Column(name = "last_modified")
    val lastModified: LocalDateTime? = null,

    @Column(name = "uri")
    val uri: String? = null,

    @Column(name = "published", nullable = false)
    val published: Boolean = false,

    @Column(name = "approved", nullable = false)
    val approved: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "specialized_type")
    val specializedType: SpecializedType? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "application_profile", nullable = false)
    val applicationProfile: ApplicationProfile = ApplicationProfile.DCAT_AP_NO,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb")
    val data: Map<String, Any?>? = null,
)

private val METADATA_FIELDS = setOf(
    "id", "catalogId", "lastModified", "uri", "published", "approved",
    "specializedType", "applicationProfile"
)

fun DatasetDBO.toEntity(mapper: ObjectMapper): DatasetEntity {
    @Suppress("UNCHECKED_CAST")
    val allFields = mapper.convertValue(this, Map::class.java) as Map<String, Any?>
    val data = allFields.filterKeys { it !in METADATA_FIELDS }.filterValues { it != null }
    return DatasetEntity(
        id = id,
        catalogId = catalogId,
        lastModified = lastModified,
        uri = uri,
        published = published ?: false,
        approved = approved ?: false,
        specializedType = specializedType,
        applicationProfile = applicationProfile,
        data = data.ifEmpty { null },
    )
}

fun DatasetEntity.toApiModel(mapper: ObjectMapper): DatasetDBO {
    val merged = mutableMapOf<String, Any?>()
    merged["id"] = id
    merged["catalogId"] = catalogId
    lastModified?.let { merged["lastModified"] = it.toString() }
    uri?.let { merged["uri"] = it }
    merged["published"] = published
    merged["approved"] = approved
    specializedType?.let { merged["specializedType"] = it.name }
    merged["applicationProfile"] = applicationProfile.name
    data?.let { merged.putAll(it) }
    return mapper.convertValue(merged, DatasetDBO::class.java)
}
