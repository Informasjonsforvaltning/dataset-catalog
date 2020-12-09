package no.fdk.dataset_catalog.utils

import no.fdk.dataset_catalog.model.*
import java.time.LocalDate
import java.util.*

val DEFINITION_EX = Definition(
    text = mapOf(Pair("nb", "Testdefinisjon")),
    remark = mapOf(Pair("nb", "Dette er en testdefinisjon")),
    source = Source(
        uri = "http://test.uri",
        prefLabel = mapOf(Pair("nb", "Testkilde"))
    ),
    targetGroup = "Dette er en enum",
    sourceRelationship = "Kildeforhold",
    range = TextAndURI(
        text = mapOf(Pair("nb", "Tekstinnhold")),
        uri = "http://test.uri"
    ),
    sources = listOf(TextAndURI(
        text = mapOf(Pair("nb", "Tekstinnhold 2")),
        uri = "http://test.uri"
    )),
    lastUpdated = LocalDate.now().toEpochDay()
)

val CONTACTPOINT_EX = ContactPoint(
    email = "contact@point.com",
    telephone = "12345678"
)

val CONCEPT_EX = Concept(
    id = "910244132",
    uri = "http://test.uri",
    identifier = "id",
    application = listOf(mapOf(Pair("nb", "For å teste ting"))),
    definition = DEFINITION_EX,
    alternativeDefinition = DEFINITION_EX,
    subject = mapOf(Pair("nb", "Tema")),
    prefLabel = mapOf(Pair("nb", "Begrep")),
    altLabel = listOf(mapOf(Pair("en", "Concept"))),
    hiddenLabel = listOf(mapOf(Pair("en", "hidden stuff"))),
    contactPoint = CONTACTPOINT_EX,
    example = mapOf(Pair("en", "an example"))
)

val SUBJECT_EX = Subject(
    uri = "http://test.uri",
    definition = mapOf(Pair("nb", "Testdefinisjon")),
    prefLabel = mapOf(Pair("nb", "Subjektnavn")),
    id = "910244132",
    identifier = "Testidentifikasjon",
    altLabel = listOf(mapOf(Pair("nb", "Alternativt subjektnavn"))),
    note = mapOf(Pair("nb", "Note to self")),
    source = "kilde",
    creator = null,
    inScheme = listOf("I skjema"),
    datasets = listOf(
        Dataset(
            id = UUID.randomUUID().toString(),
            catalogId = UUID.randomUUID().toString()
        )
    ),
)

val CONTACT_EX = Contact(
    id = "Kontaktid",
    uri = "http://test.uri",
    fullname = "Test testesen",
    email = "test@digdir.no",
    organizationName = "DigDir",
    organizationUnit = "Enhet",
    hasURL = "http://www.hjemmeside.no",
    hasTelephone = "45678912",
)

val PUBLISHER_EX = Publisher(
    uri = "http://test.uri",
    id = "PublisherID",
    name = "Publishername",
    orgPath = "orgpath/publisher",
    prefLabel = mapOf(Pair("nb", "Foretrukket navn")),
)

val SKOSCODE_EX = SkosCode(
    uri = "http://test.uri",
    code = "Skos code",
    prefLabel = mapOf(Pair("nb", "foretrukket navn"))
)

val DATATHEME_EX = DataTheme(
    id = "id",
    uri = "http://test.uri",
    code = "DataTheme code",
    pickedDate = LocalDate.now().toString(),
    startUse = LocalDate.now().toString(),
    conceptSchema = ConceptSchema(
        id = "id",
        title = mapOf(Pair("nb", "Konsepskjemanavn")),
        versioninfo = "Version info",
        versionnumber = "Version number"
    )
)

fun SKOSCONCEPT_EX(extraType: String? = null) = SkosConcept(
    uri = "http://test.uri",
    prefLabel = mapOf(Pair("nb", "Foretrukket tittel")),
    extraType = extraType,
)

val DATADISTRIBUTIONSERVICE_EX = DataDistributionService(
    id = "id",
    uri = "http://test.uri",
    title = mapOf(Pair("nb", "Tittel")),
    publisher = PUBLISHER_EX,
    description = mapOf(Pair("nb", "Beskrivelse")),
    endpointDescription = listOf(SKOSCONCEPT_EX("dcatapi:endpointDescription")),
)

val DISTRIBUTION_EX = Distribution(
    id = "id",
    uri = "http://test.uri",
    title = mapOf(Pair("nb", "Distribusjonsnavn")),
    description = mapOf(Pair("nb", "Beskrivelse")),
    downloadURL = listOf("www.hjemmeside.no"),
    accessURL = listOf("www.borteside.no"),
    license = SKOSCONCEPT_EX("dcat:Distribution"),
    openLicense = true,
    conformsTo = listOf(SKOSCONCEPT_EX("dct:conformsTo")),
    page = listOf(SKOSCONCEPT_EX("foaf:page")),
    format = listOf("Formats"),
    accessService = listOf(DATADISTRIBUTIONSERVICE_EX),
)

val PERIODOFTIME_EX = PeriodOfTime(
    id = "id",
    name = "Time period",
    startDate = LocalDate.now(),
    endDate = LocalDate.now().plusYears(1)
)

val QUALITYANNOTATION_EX = QualityAnnotation(
    inDimension = "Dimensjon",
    motivatedBy = "Motivasjon",
    hasBody = mapOf(Pair("nb", "BodyText"))
)

val REFERENCE_EX = Reference(
    referenceType = SKOSCODE_EX,
    source = SKOSCONCEPT_EX()
)

val CATALOG_EX = Catalog(
    id = UUID.randomUUID().toString(),
    uri = "http://test.uri",
    title = mapOf(Pair("nb", "Katalognavn")),
    description = mapOf(Pair("nb", "Beskrivelse")),
    publisher = PUBLISHER_EX,
    issued = LocalDate.now(),
    modified = LocalDate.now(),
    language = "nb",
    dataset = listOf(Dataset("Id", "catalogId")),
)

val TEST_DATASET_0 = Dataset(
    id = UUID.randomUUID().toString(),
    catalogId = UUID.randomUUID().toString(),
    registrationStatus = REGISTRATION_STATUS.DRAFT,
    concepts = listOf(CONCEPT_EX),
    subject = listOf(SUBJECT_EX),
    uri = "http://test.uri",
    originalUri = "http://original.uri",
    source = "kilde",
    title = mapOf(Pair("nb", "Datasett tittel")),
    description = mapOf(Pair("nb", "Beskrivelse av et datasett")),
    descriptionFormatted = mapOf(Pair("nb", "Formattert beskrivelse av et datasett")),
    objective = mapOf(Pair("nb", "Datasettets hensikt")),
    contactPoint = listOf(CONTACT_EX),
    keyword = listOf(mapOf(Pair("nb", "Nøkkelord"))),
    publisher = PUBLISHER_EX,
    issued = LocalDate.now(),
    modified = LocalDate.now(),
    language = listOf(SKOSCODE_EX),
    landingPage = listOf("www.hjemmeside.no"),
    theme = listOf(DATATHEME_EX),
    distribution = listOf(DISTRIBUTION_EX),
    sample = listOf(DISTRIBUTION_EX),
    temporal = listOf(PERIODOFTIME_EX),
    spatial = listOf(SKOSCODE_EX),
    accessRights = SKOSCODE_EX,
    accessRightsComment = listOf("Comment"),
    legalBasisForRestriction = listOf(SKOSCONCEPT_EX("dcatno:legalBasisForRestriction")),
    legalBasisForProcessing = listOf(SKOSCONCEPT_EX("dcatno:legalBasisForProcessing")),
    legalBasisForAccess = listOf(SKOSCONCEPT_EX("dcatno:legalBasisForAccess")),
    hasAccuracyAnnotation = QUALITYANNOTATION_EX,
    hasCompletenessAnnotation = QUALITYANNOTATION_EX,
    hasCurrentnessAnnotation = QUALITYANNOTATION_EX,
    hasAvailabilityAnnotation = QUALITYANNOTATION_EX,
    hasRelevanceAnnotation = QUALITYANNOTATION_EX,
    references = listOf(REFERENCE_EX),
    relations = listOf(SKOSCONCEPT_EX("dct:relation")),
    provenance = SKOSCODE_EX,
    identifier = listOf("Identifiers"),
    page = listOf("Pages"),
    accrualPeriodicity = SKOSCODE_EX,
    admsIdentifier = listOf("adms:identifier"),
    conformsTo = listOf(SKOSCONCEPT_EX("dcat:conformsTo")),
    informationModel = listOf(SKOSCONCEPT_EX("dct:informationModel")),
    qualifiedAttributions = setOf("910244132"),
    type = "type",
    catalog = CATALOG_EX,
)
