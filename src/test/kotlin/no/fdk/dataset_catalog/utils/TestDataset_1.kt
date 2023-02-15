package no.fdk.dataset_catalog.utils

import no.fdk.dataset_catalog.model.*
import no.fdk.dataset_catalog.rdf.DQV
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCTerms
import java.time.LocalDate

val CATALOG_ID = "987654321"
val CATALOG_URI = "http://localhost:5000/catalogs/$CATALOG_ID"

val DATASET_ID = "72a54592-692c-4cfa-a938-cd1a56a2ed8d"
val DATASET_URI = "$CATALOG_URI/datasets/$DATASET_ID"

val DATASET_TITLE = mapOf(Pair("nb", "Markagrensen Oslo Kommune og nærliggende kommuner"))

val DATASET_DESCRIPTION = mapOf(Pair("nb", "Datasettet avgrenser område for virkeområdet til lov 6. juni 2009 nr. 35 om naturområder i Oslo og nærliggende kommuner (markaloven) som trådte i kraft 1. september 2009. Markalovens virkeområde er fastsatt i forskrift 4. september 2015 nr. 1032 om justering av markagrensen fastlegger markalovens geografiske virkeområde med tilhørende kart."))

val PUBLISHER = Publisher(
    id="987654321",
    name="TESTETATEN",
    uri="http://data.brreg.no/enhetsregisteret/enhet/987654321"
)

val KEYWORD = listOf(
        mapOf(Pair("nb", "Bestemmelse")),
        mapOf(Pair("nb", "jord")),
        mapOf(Pair("nb", "regulering")),
        mapOf(Pair("nb", "statlig bestemmelse"))
    )

val CONCEPT = Concept(
    definition = Definition(text = mapOf(Pair("no", "alt som er registrert med et organisasjonsnummer "))),
    prefLabel = mapOf(Pair("no", "enhet")),
    uri="https://data-david.github.io/Begrep/begrep/Enhet",
    altLabel = listOf(
        mapOf(Pair("no", "orgnr")),
        mapOf(Pair("en", "orgzip"))
    )
)

val SPATIAL =listOf(
        SkosCode("http://www.geonames.org/3162656/asker.html", null, mapOf(Pair("nb", "Asker"))),
        SkosCode("http://www.geonames.org/3162212/baerum.html", null, mapOf(Pair("nb", "Bærum"))),
        SkosCode("http://www.geonames.org/3151404/hurum.html", null, mapOf(Pair("nb", "Hurum"))),
        SkosCode("http://www.geonames.org/3141104/royken.html", null, mapOf(Pair("nb", "Røyken")))
    )

val THEMES = listOf(
    DataTheme(
        uri="http://publications.europa.eu/resource/authority/data-theme/ENVI",
        code="ENVI",
        title=mapOf(Pair("nb", "Miljø"))),
    DataTheme(
        uri="http://publications.europa.eu/resource/authority/data-theme/GOVE",
        code="GOVE",
        title=mapOf(Pair("nb", "Forvaltning og offentlig støtte")))
)

val CONTACTS = listOf(
    Contact(
        uri="http://contact/someContactURI/123",
        email="digitalisering@kartverket.no",
        hasURL="http://testetaten.no/url",
        hasTelephone="22306022",
        organizationUnit="Avdeling for digitalisering"
    ),
    Contact(
        email="anonymous@anonym.org.no",
    )
)

val CONFORMS_TO = SkosConcept(
    uri="https://www.kartverket.no/geodataarbeid/standarder/sosi/",
    prefLabel=mapOf(Pair("nb","SOSI")),
    extraType=DCTerms.Standard.uri
)

val SAMPLE_DESCRIPTION = DataDistributionService(
    publisher=(PUBLISHER),
    uri="http://www.hjem.no/",
    title=mapOf(Pair("nb", "Eksempel-API")),
    description=mapOf(Pair("nb", "Dette er eksempel på et API som er referert fra en distribusjon")),
    endpointDescription = listOf(SkosConcept("http://lenke/til/en/api-beskrivelse", mapOf(Pair("nb","Oppføring i API-katalog")), FOAF.Document.uri))
)

val DISTRIBUTION = Distribution(
    uri= "$DATASET_URI/distributions/d1",
    accessURL= listOf("http://www.detteerenlenke.no/til-nedlasting",
        "http://www.detteerenannenlenke.no/til-en-annen-nedlasting",
        "http://www.detteerentredjelenke.no/til-en-tredje-nedlasting"),
    description=(mapOf(Pair("nb", "Dette er beskrivelsen av distribusjonen. Vivamus sagittis lacus vel augue laoreet rutrum faucibus dolor auctor. Vestibulum id ligula porta felis euismod semper con desbit arum. Se dokumentasjon for denne distribusjonen."))),
    conformsTo= listOf(CONFORMS_TO),
    license=SkosConcept("https://data.norge.no/nlod/no/2.0", mapOf(Pair("nb", "NODL")), extraType = DCTerms.LicenseDocument.uri),
    page= listOf(SkosConcept("http://lenke/til/mer/info",  mapOf(Pair("nb", "Dokumentasjon av distribusjonen")), extraType =  FOAF.Document.uri)),
    format=listOf("https://www.iana.org/assignments/media-types/application/json"),
    accessService=listOf(SAMPLE_DESCRIPTION)
)

val SAMPLE = Distribution(
    uri = "$DATASET_URI/samples/d2",
    description = mapOf(Pair("nb", "Dette er beskrivelsen av eksempeldataene. Vivamus sagittis lacus vel augue laoreet rutrum faucibus dolor auctor.")),
    format =  listOf("https://www.iana.org/assignments/media-types/application/rdf+xml"),
    accessURL =  listOf("http://www.detteerenlenke.no/til-nedlasting", "www.dette.kan.også/hende")
)

val TEST_DATASET_1 = Dataset(
    registrationStatus = REGISTRATION_STATUS.PUBLISH,
    catalogId = CATALOG_ID,
    id = DATASET_ID,
    uri = DATASET_URI,
    title = DATASET_TITLE,
    description = DATASET_DESCRIPTION,
    keyword = KEYWORD,
    type="Kodeliste",
    accessRights = SkosCode(
        uri="http://publications.europa.eu/resource/authority/access-right/RESTRICTED",
        code="RESTRICTED",
        prefLabel=mapOf(Pair("nb", "Begrenset"))
    ),
    publisher = PUBLISHER,
    informationModel = listOf(SkosConcept(uri="https://www.w3.org/2004/02/skos/",prefLabel=mapOf(Pair("nb","SKOS")),extraType = null)),
    temporal = listOf(PeriodOfTime(startDate = LocalDate.of(2017,1,1),endDate = LocalDate.of(2017,12,31)), PeriodOfTime(endDate=LocalDate.of(2018,10,20))),
    concepts = listOf(CONCEPT),
    accrualPeriodicity=SkosCode(uri="http://publications.europa.eu/resource/authority/frequency/ANNUAL", code="ANNUAL", prefLabel=mapOf(Pair("nb", "årlig"))),
    issued=LocalDate.of(2012, 1, 1),
    modified=LocalDate.of(2016, 9, 21),
    provenance=SkosCode(uri="http://data.brreg.no/datakatalog/provenance/vedtak", code="vedtak", prefLabel = mapOf(Pair("nb", "Vedtak"))),
    spatial=SPATIAL,
    contactPoint = CONTACTS,
    conformsTo = listOf(CONFORMS_TO),
    hasCurrentnessAnnotation=(QualityAnnotation(DQV.Currentness.uri, hasBody = mapOf(Pair("no", "Denne teksten sier noe om aktualiteten. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras mattis consectetur purus sit amet fermentum. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.")))),
    hasRelevanceAnnotation=(QualityAnnotation(DQV.Relevance.uri, hasBody = mapOf(Pair("no", "Denne teksten sier noe om relevansen. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras mattis consectetur purus sit amet fermentum. Cum sociis natoque penatibus et magnis dis parturient montes.")))),
    hasCompletenessAnnotation=(QualityAnnotation(DQV.Completeness.uri, hasBody = mapOf(Pair("no", "Denne teksten sier noe om komplettheten. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras mattis consectetur purus sit amet fermentum.")))),
    hasAccuracyAnnotation=(QualityAnnotation(DQV.Accuracy.uri, hasBody = mapOf(Pair("no", "Denne teksten sier noe om nøyaktigheten. Cras mattis consectetur purus sit.")))),
    hasAvailabilityAnnotation=(QualityAnnotation(DQV.Availability.uri, hasBody = mapOf(Pair("no", "Denne teksten sier noe om tilgjengeligheten. Vestibulum id ligula porta felis euismod semper. Duis mollis, est non commodo luctus, nisi erat porttitor ligula, eget lacinia odio sem nec elit. Cras mattis consectetur purus sit amet fermentum.")))),
    language=listOf(
        SkosCode("http://publications.europa.eu/resource/authority/language/NOR", "NOR", mapOf(Pair("nb", "Norsk"))),
        SkosCode("http://publications.europa.eu/resource/authority/language/ENG", "ENG", mapOf(Pair("nb", "Engelsk")))),
    landingPage=listOf("http://testetaten.no/landingsside/nr1", "www.this.can.happen/also"),
    theme= THEMES,
    references= listOf(Reference(SkosCode(DCTerms.references.uri, "references", mapOf(Pair("nb", "Referanse"))), SkosConcept("http://mycatalog/${CATALOG_ID}/datasets/1", prefLabel = mapOf(Pair("nb", "Referanse datasett"))))),
    relations=listOf(
            SkosConcept("http://uri-1", mapOf(Pair("nb", "label-1-nb"),Pair("en", "label-1-en"))),
            SkosConcept("http://uri-2", mapOf(Pair("nb", "label-2-nb"),Pair("en", "label-2-en")))
    ),
    identifier=listOf("42"),
    page=listOf("http://uri1"),
    admsIdentifier=listOf("http://adms.identifier.no/scheme/42"),
    legalBasisForRestriction= listOf(
        SkosConcept("https://lovdata.no/dokument/NL/lov/1992-12-04-126", mapOf(Pair("nb","Lov om arkiv [arkivlova]"))),
        SkosConcept("http://lovdata/paragraph/20", mapOf(Pair("nb","Den spesifikke loven § 20"))),
        SkosConcept("http://lovdata/paragraph/26", mapOf(Pair("nb","Den mindre spesifikke loven § 26")))
    ),
    legalBasisForProcessing= listOf(SkosConcept("http://lovdata/paragraph/2", mapOf(Pair("nb","Den andre loven med lenger tittel § 2")))),
    legalBasisForAccess= listOf(SkosConcept("http://lovdata/paragraph/10", mapOf(Pair("nb","Den siste loven med den lengste tittelen § 10")))),
    distribution=listOf(DISTRIBUTION),
    sample = listOf(SAMPLE)
)

val TEST_CATALOG_1 = Catalog(
    id=CATALOG_ID,
    title=mapOf(Pair("nb", "Tittel")),
    description=mapOf(Pair("nb", "Beskrivelse")),
    uri= CATALOG_URI,
    publisher = PUBLISHER,
)



