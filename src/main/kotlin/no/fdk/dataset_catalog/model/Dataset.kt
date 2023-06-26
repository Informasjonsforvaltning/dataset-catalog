package no.fdk.dataset_catalog.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime

data class DatasetEmbeddedWrapperDTO(
    val _embedded: Map<String, List<Dataset>>?
)

@Document(collection = "datasets")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Dataset(
    @Id
    val id: String? = null,

    val catalogId: String? = null,

    val specializedType: SpecializedType? = null,

    @JsonProperty(value = "_lastModified")
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val lastModified: LocalDateTime? = null,

    val registrationStatus: REGISTRATION_STATUS? = null,

    val concepts: Collection<Concept>? = null,

    val uri: String? = null,

    val originalUri: String? = null,

//    dct:source
    val source: String? = null,

    // dct:title
    // Norwegian: Tittel
    val title: Map<String, String>? = null,

    //dct:description
    //Norwegian: Beskrivelse
    val description: Map<String, String>? = null,

    //dcat:contactPoint
    //Norwegian: Kontaktpunkt
    val contactPoint: List<Contact>? = null,

    //dcat:keyword
    //Norwegian: Emneord
    val keyword: List<Map<String, String>>? = null,

    //dct:publisher
    //Norwegian: Utgiver
    val publisher: Publisher? = null,

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
    val language: List<SkosCode>? = null,

    //dcat:landingPage
    //Norwegian: Landingsside
    val landingPage: List<String>? = null,

    //dcat:theme
    //Norwegian: Tema
    val theme: List<DataTheme>? = null,

    //dcat:distribution
    //Norwegian: Datasett distribusjon
    val distribution: List<Distribution>? = null,

    //adms:sample
    //Norwegian: Eksempeldata
    val sample: List<Distribution>? = null,

    //dct:temporal
    //Norwegian: tidsperiode
    val temporal: List<PeriodOfTime>? = null,

    //dct:spatial
    //Norwegian: dekningsområde
    val spatial: List<SkosCode>? = null,

    //dct:accessRights
    //Norwegian: tilgangsnivå
    val accessRights: SkosCode? = null,

    // dcatno:legalBasisForRestriction
    //Norwegian: skjermingshjemmel
    val legalBasisForRestriction: List<SkosConcept>? = null,

    // dcatno:legalBasisForProcessing
    //Norwegian: behanlingsgrunnlag
    val legalBasisForProcessing: List<SkosConcept>? = null,

    // dcatno:legalBasisForAccess
    //Norwegian: utleveringshjemmel
    val legalBasisForAccess: List<SkosConcept>? = null,

    // dcatno:hasXXXXAnnotation
    val hasAccuracyAnnotation: QualityAnnotation? = null,
    val hasCompletenessAnnotation: QualityAnnotation? = null,
    val hasCurrentnessAnnotation: QualityAnnotation? = null,
    val hasAvailabilityAnnotation: QualityAnnotation? = null,
    val hasRelevanceAnnotation: QualityAnnotation? = null,

    //dct:references
    //Norwegian: Referer til.
    val references: List<Reference>? = null,

    //dct:relation
    //Generic relations to resources
    val relations: List<SkosConcept>? = null,

    //dct:provenance
    //Norwegian: Opphav
    val provenance: SkosCode? = null,

    //dct:identifier
    //Norwegian: identifikator
    val identifier: List<String>? = null,

    //foaf:page
    //Norwegian: dokumentasjon
    val page: List<String>? = null,

    //dct:accrualPeriodicity
    //Norwegian: frekvens
    val accrualPeriodicity: SkosCode? = null,

    //adms:identifier
    //Norwegian: annen identifikator
    val admsIdentifier: List<String>? = null,

    //dcat:conformsTo
    //Norwegian: I samsvar med
    val conformsTo: List<SkosConcept>? = null,

    // NEW FIELDS DCAT-AP-NO 1.2?
    // dct: informationModel
    // Norwegian: informasjonsmodell
//    a: SkosConcept
    val informationModel: List<SkosConcept>? = null,

    // prov:qualifiedAttribution
    // Norwegian: innholdsleverandører
    val qualifiedAttributions: Set<String>? = null,

    //dct:type
    //Norwegian: type
    val type: String? = null,

//    dcat:catalogue
    val catalog: Catalog? = null,

    // all series that this dataset is a part of
    val inSeries: String? = null,

    // datasets in this series and their order index
    val seriesDatasetOrder: Map<String, Int>? = null
    )

enum class REGISTRATION_STATUS {
    DRAFT, APPROVE, PUBLISH
}

enum class SpecializedType {
    SERIES
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Concept(
    val uri: String? = null,
    val identifier: String? = null,
    val definition: Definition? = null,
    val prefLabel: Map<String, String>? = null,
    val altLabel: List<Map<String, String>>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Definition(
    val text: Map<String, String>? = null,
    val remark: Map<String, String>? = null,
    val source: Source? = null,
    val targetGroup: String? = null, // TODO this is string-enum
    val sourceRelationship: String? = null,
    val range: TextAndURI? = null,
    val sources: List<TextAndURI>? = null,
    val lastUpdated: Long? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Source(
    val uri: String? = null,
    val prefLabel: Map<String, String>? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class TextAndURI(
    val text: Map<String, String>? = null,
    val uri: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Contact(
    val id: String? = null,
    val uri: String? = null,
    val fullname: String? = null,
    val email: String? = null,
    val organizationName: String? = null,
    val organizationUnit: String? = null,
    val hasURL: String? = null,
    val hasTelephone: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Publisher(
    val uri: String? = null,
    val id: String? = null,
    val name: String? = null,
    val orgPath: String? = null,
    val prefLabel: Map<String, String>? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class SkosCode(
    val uri: String? = null,
    val code: String? = null,
    val prefLabel: Map<String, String>? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DataTheme(
    val id: String? = null,
    val uri: String? = null,
    val code: String? = null,
    val pickedDate: String? = null,
    val startUse: String? = null,
    val title: Map<String, String>? = null,
    val conceptSchema: ConceptSchema? = null,
    val numberOfHits: Int? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Distribution(
    val id: String? = null,
    val uri: String? = null,
    val title: Map<String, String>? = null,
    val description: Map<String, String>? = null,
    val downloadURL: List<String>? = null,
    val accessURL: List<String>? = null,
    val license: SkosConcept? = null,
    val openLicense: Boolean? = null,
    val conformsTo: List<SkosConcept>? = null,
    val page: List<SkosConcept>? = null,
    val format: List<String>? = null,
    val mediaType: List<String>? = null,
    val accessService: List<DataDistributionService>? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class PeriodOfTime(
    val id: String? = null,
    val name: String? = null,
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val startDate: LocalDate? = null,
    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val endDate: LocalDate? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class SkosConcept(
    val uri: String? = null,
    val prefLabel: Map<String, String>? = null,
    val extraType: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class QualityAnnotation(
    val inDimension: String? = null,
    val motivatedBy: String? = null,
    val hasBody: Map<String, String>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Reference(
    val referenceType: SkosCode? = null,
    val source: SkosConcept? = null // link to Dataset
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ConceptSchema(
    val id: String? = null,
    val title: Map<String, String>? = null,
    val versioninfo: String? = null,
    val versionnumber: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DataDistributionService(
    val id: String? = null,
    val uri: String? = null,

    //dct:title
    //Norwgian: Tittel
    val title: Map<String, String>? = null,

    //dct:publisher
    //Norwegian: Utgiver
    val publisher: Publisher? = null,

    //dct:description
    //Norwegian: Beskrivelse
    val description: Map<String, String>? = null,

    //dcatapi:endpointDescription
    val endpointDescription: List<SkosConcept>? = null,
)