package no.fdk.dataset_catalog.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.springframework.data.annotation.*
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime

@Document(collection = "datasets")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DatasetDBO(
    @Id
    val id: String,
    val catalogId: String,

    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val lastModified: LocalDateTime?,
    val uri: String?,

    val published: Boolean? = false,
    val approved: Boolean? = false,

    val originalUri: String? = null,
    val specializedType: SpecializedType? = null,
    val concepts: Set<String>? = null,

    val title: LocalizedStrings? = null,
    val description: LocalizedStrings? = null,

    val contactPoints: List<ContactPoint>? = null,
    val keywords: LocalizedStringLists? = null,

    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val issued: LocalDate? = null,

    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val modified: LocalDate? = null,

    val language: List<String>? = null,
    val landingPage: List<String>? = null,

    val euDataTheme: Set<String>? = null,
    val losTheme: Set<String>? = null,

    val distribution: List<DistributionDBO>? = null,
    val sample: List<DistributionDBO>? = null,

    val temporal: List<PeriodOfTimeDBO>? = null,
    val spatial: List<String>? = null,

    val accessRight: String? = null,

    val legalBasisForRestriction: List<UriWithLabel>? = null,
    val legalBasisForProcessing: List<UriWithLabel>? = null,
    val legalBasisForAccess: List<UriWithLabel>? = null,

    val accuracy: QualityAnnotationDBO? = null,
    val completeness: QualityAnnotationDBO? = null,
    val currentness: QualityAnnotationDBO? = null,
    val availability: QualityAnnotationDBO? = null,
    val relevance: QualityAnnotationDBO? = null,

    val references: List<ReferenceDBO>? = null,
    val relatedResources: List<UriWithLabel>? = null,
    val provenance: String? = null,
    val frequency: String? = null,

    val conformsTo: List<UriWithLabel>? = null,
    val informationModelsFromOtherSources: List<UriWithLabel>? = null,
    val informationModelsFromFDK: List<String>? = null,
    val qualifiedAttributions: Set<String>? = null,
    val type: String? = null,

    val inSeries: String? = null,
    val seriesDatasetOrder: Map<String, Int>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DatasetToCreate(
    val approved: Boolean = false,
    val originalUri: String? = null,
    val specializedType: SpecializedType? = null,
    val concepts: Set<String>? = null,

    val title: LocalizedStrings? = null,
    val description: LocalizedStrings? = null,

    val contactPoints: List<ContactPoint>? = null,
    val keywords: LocalizedStringLists? = null,

    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val issued: LocalDate? = null,

    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val modified: LocalDate? = null,

    val language: List<String>? = null,
    val landingPage: List<String>? = null,

    val euDataTheme: Set<String>? = null,
    val losTheme: Set<String>? = null,

    val distribution: List<DistributionDBO>? = null,
    val sample: List<DistributionDBO>? = null,

    val temporal: List<PeriodOfTimeDBO>? = null,
    val spatial: List<String>? = null,

    val accessRight: String? = null,

    val legalBasisForRestriction: List<UriWithLabel>? = null,
    val legalBasisForProcessing: List<UriWithLabel>? = null,
    val legalBasisForAccess: List<UriWithLabel>? = null,

    val accuracy: QualityAnnotationDBO? = null,
    val completeness: QualityAnnotationDBO? = null,
    val currentness: QualityAnnotationDBO? = null,
    val availability: QualityAnnotationDBO? = null,
    val relevance: QualityAnnotationDBO? = null,

    val references: List<ReferenceDBO>? = null,
    val relatedResources: List<UriWithLabel>? = null,
    val provenance: String? = null,
    val frequency: String? = null,

    val conformsTo: List<UriWithLabel>? = null,
    val informationModelsFromOtherSources: List<UriWithLabel>? = null,
    val informationModelsFromFDK: List<String>? = null,
    val qualifiedAttributions: Set<String>? = null,
    val type: String? = null,

    val inSeries: String? = null,
    val seriesDatasetOrder: Map<String, Int>? = null
)

enum class SpecializedType {
    SERIES
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ContactPoint(
    val name: LocalizedStrings? = null,
    val email: String? = null,
    val url: String? = null,
    val phone: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DistributionDBO(
    val title: LocalizedStrings? = null,
    val description: LocalizedStrings? = null,
    val downloadURL: List<String>? = null,
    val accessURL: List<String>? = null,
    val license: String? = null,
    val conformsTo: List<UriWithLabel>? = null,
    val page: List<String>? = null,
    val format: List<String>? = null,
    val mediaType: List<String>? = null,
    var accessServices: Set<String>? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class PeriodOfTimeDBO(
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val startDate: LocalDate? = null,
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val endDate: LocalDate? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class QualityAnnotationDBO(
    val motivatedBy: String? = null,
    val hasBody: LocalizedStrings? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ReferenceDBO(
    val referenceType: String? = null,
    val source: String? = null // referenced dataset uri
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class LocalizedStrings(
    val nb: String? = null,
    val nn: String? = null,
    val en: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class LocalizedStringLists(
    val nb: List<String>? = null,
    val nn: List<String>? = null,
    val en: List<String>? = null
)

data class User(
    val id: String,
    val name: String? = null,
    val email: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class UriWithLabel(
    val uri: String? = null,
    val prefLabel: LocalizedStrings? = null,
)
