package no.fdk.dataset_catalog.utils

import no.fdk.dataset_catalog.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

val CONCEPT_EX = "http://test.uri"

val CONTACT_EX = ContactPoint(
    email = "test@digdir.no",
    name = LocalizedStrings(nb = "Enhet"),
    url = "http://www.hjemmeside.no",
    phone = "45678912",
)

val SKOSCODE_EX = "http://test.uri"

val REFERENCETYPE_EX = "isReplacedBy"

val DISTRIBUTION_EX = DistributionDBO(
    title = LocalizedStrings(nb = "Distribusjonsnavn"),
    description = LocalizedStrings(nb = "Beskrivelse"),
    downloadURL = listOf("www.hjemmeside.no"),
    accessURL = listOf("www.borteside.no"),
    license = "https://license.com",
    conformsTo = listOf(UriWithLabel("http://test.uri")),
    format = listOf("Formats"),
    mediaType = listOf("MediaTypes"),
    accessServices = setOf("http://test.uri")
)

val SAMPLE_EX = DistributionDBO(
    title = LocalizedStrings(nb = "Distribusjonsnavn"),
    description = LocalizedStrings(nb = "Beskrivelse"),
    downloadURL = listOf("www.hjemmeside.no"),
    accessURL = listOf("www.borteside.no"),
)

val PERIODOFTIME_EX = PeriodOfTimeDBO(
    startDate = LocalDate.now(),
    endDate = LocalDate.now().plusYears(1)
)

val QUALITYANNOTATION_EX = QualityAnnotationDBO(
    hasBody = LocalizedStrings(nb = "BodyText")
)

val REFERENCE_EX = ReferenceDBO(
    referenceType = REFERENCETYPE_EX,
    source = "http://test.uri"
)

val TEST_DATASET_0 = DatasetDBO(
    id = UUID.randomUUID().toString(),
    catalogId = UUID.randomUUID().toString(),
    published = false,
    approved = false,
    lastModified = LocalDateTime.now(),
    concepts = setOf(CONCEPT_EX),
    uri = "http://test.uri",
    originalUri = "http://original.uri",
    title = LocalizedStrings(nb = "Datasett tittel"),
    description = LocalizedStrings(nb = "Beskrivelse av et datasett"),
    contactPoints = listOf(CONTACT_EX),
    keywords = LocalizedStringLists(nb = listOf("Nøkkelord")),
    issued = LocalDate.now(),
    modified = LocalDate.now(),
    language = listOf("http://publications.europa.eu/resource/authority/language/NNO"),
    landingPage = listOf("www.hjemmeside.no"),
    distribution = listOf(DISTRIBUTION_EX),
    sample = listOf(SAMPLE_EX),
    temporal = listOf(PERIODOFTIME_EX),
    spatial = listOf(SKOSCODE_EX),
    accessRight = SKOSCODE_EX,
    legalBasisForRestriction = listOf(UriWithLabel(uri = "http://test.uri")),
    legalBasisForProcessing = listOf(UriWithLabel(uri = "http://test.uri")),
    legalBasisForAccess = listOf(UriWithLabel(uri = "http://test.uri")),
    accuracy = QUALITYANNOTATION_EX,
    completeness = QUALITYANNOTATION_EX,
    currentness = QUALITYANNOTATION_EX,
    availability = QUALITYANNOTATION_EX,
    relevance = QUALITYANNOTATION_EX,
    references = listOf(REFERENCE_EX),
    relatedResources = listOf(UriWithLabel(uri = "http://test.uri")),
    provenance = SKOSCODE_EX,
    frequency = SKOSCODE_EX,
    conformsTo = listOf(UriWithLabel(uri = "http://test.uri")),
    informationModelsFromOtherSources = listOf(UriWithLabel(uri = "http://test.uri")),
    qualifiedAttributions = setOf("910244132"),
    type = "type",
)
