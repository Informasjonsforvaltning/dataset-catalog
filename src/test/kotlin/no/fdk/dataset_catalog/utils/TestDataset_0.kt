package no.fdk.dataset_catalog.utils

import no.fdk.dataset_catalog.model.*
import java.time.LocalDate
import java.util.*

val CONCEPT_EX = Concept(
    uri = "http://test.uri",
)

val CONTACT_EX = Contact(
    email = "test@digdir.no",
    organizationUnit = "Enhet",
    hasURL = "http://www.hjemmeside.no",
    hasTelephone = "45678912",
)

val SKOSCODE_EX = SkosCode(
    uri = "http://test.uri",
)

val REFERENCETYPE_EX = SkosCode(
    code = "https://ref.code"
)

val DISTRIBUTION_EX = Distribution(
    title = mapOf(Pair("nb", "Distribusjonsnavn")),
    description = mapOf(Pair("nb", "Beskrivelse")),
    downloadURL = listOf("www.hjemmeside.no"),
    accessURL = listOf("www.borteside.no"),
    license = SkosConcept(uri = "https://license.com"),
    conformsTo = listOf(SkosConcept("http://test.uri")),
    format = listOf("Formats"),
    mediaType = listOf("MediaTypes"),
    accessServiceUris = setOf("http://test.uri"),
    accessService = listOf(DataDistributionService(uri = "http://test.uri"))

)

val SAMPLE_EX = Distribution(
    title = mapOf(Pair("nb", "Distribusjonsnavn")),
    description = mapOf(Pair("nb", "Beskrivelse")),
    downloadURL = listOf("www.hjemmeside.no"),
    accessURL = listOf("www.borteside.no"),
)

val PERIODOFTIME_EX = PeriodOfTime(
    startDate = LocalDate.now(),
    endDate = LocalDate.now().plusYears(1)
)

val QUALITYANNOTATION_EX = QualityAnnotation(
    hasBody = mapOf(Pair("nb", "BodyText"))
)

val REFERENCE_EX = Reference(
    referenceType = REFERENCETYPE_EX,
    source = SkosConcept(uri = "http://test.uri")
)

val TEST_DATASET_0 = Dataset(
    id = UUID.randomUUID().toString(),
    catalogId = UUID.randomUUID().toString(),
    registrationStatus = REGISTRATION_STATUS.DRAFT,
    concepts = listOf(CONCEPT_EX),
    uri = "http://test.uri",
    originalUri = "http://original.uri",
    title = mapOf(Pair("nb", "Datasett tittel")),
    description = mapOf(Pair("nb", "Beskrivelse av et datasett")),
    contactPoint = listOf(CONTACT_EX),
    keyword = listOf(mapOf(Pair("nb", "Nøkkelord"))),
    issued = LocalDate.now(),
    modified = LocalDate.now(),
    language = listOf(SkosCode(uri = "http://publications.europa.eu/resource/authority/language/NNO", code = "NNO")),
    landingPage = listOf("www.hjemmeside.no"),
    distribution = listOf(DISTRIBUTION_EX),
    sample = listOf(SAMPLE_EX),
    temporal = listOf(PERIODOFTIME_EX),
    spatial = listOf(SKOSCODE_EX),
    accessRights = SKOSCODE_EX,
    legalBasisForRestriction = listOf(SkosConcept(uri = "http://test.uri")),
    legalBasisForProcessing = listOf(SkosConcept(uri = "http://test.uri")),
    legalBasisForAccess = listOf(SkosConcept(uri = "http://test.uri")),
    hasAccuracyAnnotation = QUALITYANNOTATION_EX,
    hasCompletenessAnnotation = QUALITYANNOTATION_EX,
    hasCurrentnessAnnotation = QUALITYANNOTATION_EX,
    hasAvailabilityAnnotation = QUALITYANNOTATION_EX,
    hasRelevanceAnnotation = QUALITYANNOTATION_EX,
    references = listOf(REFERENCE_EX),
    relations = listOf(SkosConcept(uri = "http://test.uri")),
    provenance = SKOSCODE_EX,
    accrualPeriodicity = SKOSCODE_EX,
    conformsTo = listOf(SkosConcept(uri = "http://test.uri")),
    informationModel = listOf(SkosConcept(uri = "http://test.uri")),
    qualifiedAttributions = setOf("910244132"),
    type = "type",
)
