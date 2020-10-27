package no.fdk.dataset_catalog.model

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

@Document( collection = "datasets" )
data class Dataset(
    @Id
    val id: String? = null,

    val catalogId: String? = null,

    @JsonProperty(value = "_lastModified")
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    val lastModified: LocalDateTime? = null,

    val registrationStatus: REGISTRATION_STATUS? = null,

    val concepts: Collection<Concept>? = null,

    val subjects: Collection<Subject>? = null,

    val uri: String? = null,

    val originalUri: String? = null,

    val source: String? = null,

    val harvest: HarvestMetadata? = null,

    // dct:title
    // Norwegian: Tittel
    val title: Map<String, String>? = null,

    //dct:description
    //Norwegian: Beskrivelse
    val description: Map<String, String>? = null,
    val descriptionFormatted: Map<String, String>? = null,

    //dcatno:objective
    //Norwegian: Formål
    val objective: Map<String, String>? = null,

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
    @JsonDeserialize(using = LocalDateDeserializer::class)
    @JsonSerialize(using = LocalDateSerializer::class)
    val issued: LocalDate? = null,

    //dct:modified
    //Norwegian: Modifiseringsdato
    @JsonDeserialize(using = LocalDateDeserializer::class)
    @JsonSerialize(using = LocalDateSerializer::class)
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

    //dcatno:accessRightsComment
    //Norwegian: Skjermingshjemmel.
    //Norwegian extension to the dcat standard. Recommended used with accesRights.
    @Deprecated("")
    val accessRightsComment: List<String>? = null,

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
    //Norwegian: Refererer til.
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

    //dct:subject
    //Norwegian: begrep
    val subject: List<Subject>? = null,

    //adms:identifier
    //Norwegian: annen identifikator
    val admsIdentifier: List<String>? = null,

    //dcat:conformsTo
    //Norwegian: I samsval med
    val conformsTo: List<SkosConcept>? = null,

    // NEW FIELDS DCAT-AP-NO 1.2?
    // dct: informationModel
    // Norwegian: informasjonsmodell
    val informationModel: List<SkosConcept>? = null,

    // prov:qualifiedAttribution
    // Norwegian: innholdsleverandører
    val qualifiedAttributions: Set<String>? = null,

    //dct:type
    //Norwegian: type
    val type: String? = null,

    val catalog: Catalog? = null,

    )

enum class REGISTRATION_STATUS {
    DRAFT, APPROVE, PUBLISH
}

data class Concept (
    val id: String? = null,

    val uri: String? = null,

    val identifier: String? = null,

    val application: List<Map<String, String>> = emptyList(),

    val definition: Definition? = null,

    val alternativeDefinition: Definition? = null,

    val subject: Map<String, String> = emptyMap(),

    val prefLabel: Map<String, String> = emptyMap(),

    val altLabel: List<Map<String, String>> = emptyList(),

    val hiddenLabel: List<Map<String, String>> = emptyList(),

    val contactPoint: ContactPoint? = null,

    val example: Map<String, String> = emptyMap(),
)

data class ContactPoint(
    val email: String? = null,
    val telephone: String? = null,
)

data class Definition(
    val text: Map<String, String> = emptyMap(),
    val remark: Map<String, String> = emptyMap(),
    val source: Source? = null,
    val targetGroup: String? = null, // TODO this is string-enum
    val sourceRelationship: String? = null,
    val range: TextAndURI? = null,
    val sources: List<TextAndURI> = emptyList(),
    val lastUpdated: Long? = null,
)

data class Source(
    val uri: String? = null,
    val prefLabel: Map<String, String>? = null,
)

data class TextAndURI(
    val text: Map<String, String> = emptyMap(),
    val uri: String? = null,
)

data class HarvestMetadata (
    val firstHarvested: Long? = null,
    val lastHarvested: Long? = null,
    val lastChanged: Long? = null,
    val changed: List<Long> = emptyList(),
)

data class Contact (
    val id: String? = null,
    val uri: String? = null,
    val fullname: String? = null,
    val email: String? = null,
    val organizationName: String? = null,
    val organizationUnit: String? = null,
    val hasURL: String? = null,
    val hasTelephone: String? = null,
)

data class Publisher (
    val uri: String? = null,
    val id: String? = null,
    val name: String? = null,
    val orgPath: String? = null,
    val prefLabel: Map<String, String> = emptyMap(),
)

data class SkosCode (
    val uri: String? = null,
    val code: String? = null,
    val prefLabel: Map<String, String> = emptyMap(),
)

data class DataTheme (
    val id: String? = null,
    val uri: String? = null,
    val code: String? = null,
    val pickedDate: String? = null,
    val startUse: String? = null,
    val title: Map<String, String> = emptyMap(),
    val conceptSchema: ConceptSchema? = null,
    val numberOfHits: Int? = null,
)

data class Distribution (
    val id: String? = null,
    val uri: String? = null,
    val title: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap(),
    val downloadURL: List<String> = emptyList(),
    val accessURL: List<String> = emptyList(),
    val license: SkosConcept? = null,
    val openLicense: Boolean? = null,
    val conformsTo: List<SkosConcept> = emptyList(),
    val page: List<SkosConcept> = emptyList(),
    val format: List<String> = emptyList(),
    val accessService: List<DataDistributionService> = emptyList(),
)

data class PeriodOfTime (
    val id: String? = null,
    val name: String? = null,
    @JsonDeserialize(using = LocalDateDeserializer::class)
    @JsonSerialize(using = LocalDateSerializer::class)
    val startDate: LocalDate? = null,
    @JsonDeserialize(using = LocalDateDeserializer::class)
    @JsonSerialize(using = LocalDateSerializer::class)
    val endDate: LocalDate? = null,
)

data class SkosConcept (
    val uri: String? = null,
    val prefLabel: MutableMap<String, String> = mutableMapOf(),
    val extraType: String? = null,
)

data class QualityAnnotation (
    val inDimension: String? = null,
    val motivatedBy: String? = null,
    val hasBody: Map<String, String> = emptyMap()
)

data class Reference (
    val referenceType: SkosCode? = null,
    val source: SkosConcept? = null // link to Dataset
)

data class ConceptSchema (
    val id: String? = null,
    val title: Map<String, String> = emptyMap(),
    val versioninfo: String? = null,
    val versionnumber: String? = null,
)

data class DataDistributionService (
    val id: String? = null,
    val uri: String? = null,

    //dct:title
    //Norwgian: Tittel
    val title: Map<String, String> = emptyMap(),

    //dct:publisher
    //Norwegian: Utgiver
    val publisher: Publisher? = null,

    //dct:description
    //Norwegian: Beskrivelse
    val description: Map<String, String> = emptyMap(),

    //dcatapi:endpointDescription
    val endpointDescription: List<SkosConcept> = emptyList(),
)

data class Organization (
    @JsonProperty("organizationId")
    val organizationId: String? = null,

    @JsonProperty("name")
    val name: String? = null,

    @JsonProperty("allowDelegatedRegistration")
    val allowDelegatedRegistration: Boolean? = null,
)
