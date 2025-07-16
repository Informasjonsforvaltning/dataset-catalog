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
    val lastModified: LocalDateTime? = null,
    val originalUri: String? = null,
    val uri: String? = null,

    val specializedType: SpecializedType? = null,

    val published: Boolean = false,
    val approved: Boolean = false,
    val concepts: Set<String>? = null,


    // dct:title
    // Norwegian: Tittel
    val title: LocalizedStrings? = null,

    //dct:description
    //Norwegian: Beskrivelse
    val description: LocalizedStrings? = null,

    //dcat:contactPoint
    //Norwegian: Kontaktpunkt
    val contactPoints: List<ContactPoint>? = null,

    //dcat:keyword
    //Norwegian: Emneord
    val keywords: LocalizedStringLists? = null,

    //dct:issued
    //Norwegian: Utgivelsesdato
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val issued: LocalDate? = null,

    //dct:modified
    //Norwegian: Modifiseringsdato
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val modified: LocalDate? = null,

    //dct:language
    //Norwegian: Språk
    val language: List<String>? = null,

    //dcat:landingPage
    //Norwegian: Landingsside
    val landingPage: List<String>? = null,


    // dcat:theme, eu-vocabulary
    val euDataTheme: Set<String>? = null,
    // dcat:theme, norwegian expansion of the eu-vocabulary
    val losTheme: Set<String>? = null,


    //dcat:distribution
    //Norwegian: Datasett distribusjon
    val distribution: List<DistributionDBO>? = null,

    //adms:sample
    //Norwegian: Eksempeldata
    val sample: List<DistributionDBO>? = null,

    //dct:temporal
    //Norwegian: tidsperiode
    val temporal: List<PeriodOfTimeDBO>? = null,

    //dct:spatial
    //Norwegian: dekningsområde
    val spatial: List<String>? = null,

    //dct:accessRights
    //Norwegian: tilgangsnivå
    val accessRight: String? = null,

    // dcatno:legalBasisForRestriction
    //Norwegian: skjermingshjemmel
    val legalBasisForRestriction: List<UriWithLabel>? = null,

    // dcatno:legalBasisForProcessing
    //Norwegian: behanlingsgrunnlag
    val legalBasisForProcessing: List<UriWithLabel>? = null,

    // dcatno:legalBasisForAccess
    //Norwegian: utleveringshjemmel
    val legalBasisForAccess: List<UriWithLabel>? = null,

    // dqv:hasXXXXQualityAnnotation
    val accuracy: QualityAnnotationDBO? = null,
    val completeness: QualityAnnotationDBO? = null,
    val currentness: QualityAnnotationDBO? = null,
    val availability: QualityAnnotationDBO? = null,
    val relevance: QualityAnnotationDBO? = null,

    //dct:references
    //Norwegian: Referer til.
    val references: List<ReferenceDBO>? = null,

    //dct:relation
    //Generic relations to resources
    val relatedResources: List<UriWithLabel>? = null,

    //dct:provenance
    //Norwegian: Opphav
    val provenance: String? = null,

    //dct:accrualPeriodicity
    //Norwegian: frekvens
    val frequency: String? = null,

    //dcat:conformsTo
    //Norwegian: I samsvar med
    val conformsTo: List<UriWithLabel>? = null,

    // NEW FIELDS DCAT-AP-NO 1.2?
    // dct: informationModel
    // Norwegian: informasjonsmodell
//    a: SkosConcept
    val informationModelsFromOtherSources: List<UriWithLabel>? = null,
    val informationModelsFromFDK: List<String>? = null,

    // prov:qualifiedAttribution
    // Norwegian: innholdsleverandører
    val qualifiedAttributions: Set<String>? = null,

    //dct:type
    //Norwegian: type
    val type: String? = null,

    // all series that this dataset is a part of
    val inSeries: String? = null,

    // datasets in this series and their order index
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
    val hasBody: Map<String, String>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ReferenceDBO(
    val referenceType: String? = null,
    val source: String? = null // referenced dataset uri
)

data class LocalizedStrings(
    val nb: String? = null,
    val nn: String? = null,
    val en: String? = null,
)

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
    val prefLabel: Map<String, String>? = null,
)