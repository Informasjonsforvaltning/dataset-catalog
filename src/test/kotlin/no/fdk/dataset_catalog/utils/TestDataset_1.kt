package no.fdk.dataset_catalog.utils

import no.fdk.dataset_catalog.model.*
import java.time.LocalDate
import java.time.LocalDateTime

val CATALOG_ID = "987654321"
val CATALOG_URI = "http://localhost:5050/catalogs/$CATALOG_ID"

val DATASET_ID = "72a54592-692c-4cfa-a938-cd1a56a2ed8d"
val DATASET_URI = "$CATALOG_URI/datasets/$DATASET_ID"

val DATASET_TITLE = LocalizedStrings(nb = "Markagrensen Oslo Kommune og nærliggende kommuner")

val DATASET_DESCRIPTION = LocalizedStrings(nb = "Datasettet avgrenser område for virkeområdet til lov 6. juni 2009 nr. 35 om naturområder i Oslo og nærliggende kommuner (markaloven) som trådte i kraft 1. september 2009. Markalovens virkeområde er fastsatt i forskrift 4. september 2015 nr. 1032 om justering av markagrensen fastlegger markalovens geografiske virkeområde med tilhørende kart.")

val KEYWORDS = LocalizedStringLists(
    nb = listOf("Bestemmelse", "jord", "regulering", "statlig bestemmelse")
)

val CONCEPT = "https://data-david.github.io/Begrep/begrep/Enhet"

val SPATIAL =listOf(
    "http://www.geonames.org/3162656/asker.html",
    "http://www.geonames.org/3162212/baerum.html",
    "http://www.geonames.org/3151404/hurum.html",
    "http://www.geonames.org/3141104/royken.html"
)

val THEMES = setOf(
    "http://publications.europa.eu/resource/authority/data-theme/ENVI",
    "http://publications.europa.eu/resource/authority/data-theme/GOVE",
)

val CONTACTS = listOf(
    ContactPoint(
        email="digitalisering@kartverket.no",
        url="http://testetaten.no/url",
        phone="22 30 60 22",
        name= LocalizedStrings(nb = "Avdeling for digitalisering")
    ),
    ContactPoint(
        email="anonymous@anonym.org.no",
    )
)

val CONFORMS_TO = UriWithLabel(
    uri="https://www.kartverket.no/geodataarbeid/standarder/sosi/",
    prefLabel=LocalizedStrings(nb = "SOSI")
)

val RIGHTS = RightsDBO(
  type = "https://w3id.org/mobilitydcat-ap/conditions-for-access-and-usage"
)

val DISTRIBUTION = DistributionDBO(
    accessURL= listOf("http://www.detteerenlenke.no/til-nedlasting",
        "http://www.detteerenannenlenke.no/til-en-annen-nedlasting",
        "http://www.detteerentredjelenke.no/til-en-tredje-nedlasting"),
    description=LocalizedStrings(nb = "Dette er beskrivelsen av distribusjonen. Vivamus sagittis lacus vel augue laoreet rutrum faucibus dolor auctor. Vestibulum id ligula porta felis euismod semper con desbit arum. Se dokumentasjon for denne distribusjonen."),
    conformsTo= listOf(CONFORMS_TO),
    license="https://data.norge.no/nlod/no/2.0",
    page= listOf("http://lenke/til/mer/info"),
    format=listOf("http://publications.europa.eu/resource/authority/file-type/JSON"),
    mediaType=listOf("https://www.iana.org/assignments/media-types/application/json"),
    accessServices = setOf("http://www.hjem.no/", "http://www.hjem2.no/"),
    mobilityDataStandard = "https://w3id.org/mobilitydcat-ap/mobility-data-standard/datex-II",
    rights = RIGHTS
)

val SAMPLE = DistributionDBO(
    description = LocalizedStrings(nb = "Dette er beskrivelsen av eksempeldataene. Vivamus sagittis lacus vel augue laoreet rutrum faucibus dolor auctor."),
    format =  listOf("http://publications.europa.eu/resource/authority/file-type/RDF"),
    mediaType =  listOf("https://www.iana.org/assignments/media-types/application/rdf+xml"),
    accessURL =  listOf("http://www.detteerenlenke.no/til-nedlasting", "www.dette.kan.også/hende")
)

val TEST_DATASET_1 = DatasetDBO(
    published = true,
    approved = true,
    catalogId = CATALOG_ID,
    id = DATASET_ID,
    uri = DATASET_URI,
    lastModified= LocalDateTime.of(2016, 9, 21, 0, 0, 0),
    title = DATASET_TITLE,
    description = DATASET_DESCRIPTION,
    keywords = KEYWORDS,
    type="Kodeliste",
    accessRight = "http://publications.europa.eu/resource/authority/access-right/RESTRICTED",
    informationModelsFromOtherSources = listOf(UriWithLabel(uri="",prefLabel=LocalizedStrings(nb = "SKOS"))),
    informationModelsFromFDK = listOf(
        "https://raw.githubusercontent.com/Informasjonsforvaltning/model-publisher/master/src/model/catalog-of-models-for-specifications.ttl#dqv-ap-no-model",
        "https://raw.githubusercontent.com/Informasjonsforvaltning/fdk-testdata/master/testdata/SkatvalModellkatalog.ttl#AdresseModell"),
    temporal = listOf(PeriodOfTimeDBO(startDate = LocalDate.of(2017,1,1),endDate = LocalDate.of(2017,12,31)), PeriodOfTimeDBO(endDate=LocalDate.of(2018,10,20))),
    concepts = setOf(CONCEPT),
    frequency="http://publications.europa.eu/resource/authority/frequency/ANNUAL",
    issued=LocalDate.of(2012, 1, 1),
    modified=LocalDate.of(2016, 9, 21),
    provenance="http://data.brreg.no/datakatalog/provenance/vedtak",
    spatial=SPATIAL,
    contactPoints = CONTACTS,
    conformsTo = listOf(CONFORMS_TO),
    currentness=(QualityAnnotationDBO(hasBody = LocalizedStrings(nb = "Denne teksten sier noe om aktualiteten. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras mattis consectetur purus sit amet fermentum. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus."))),
    relevance=(QualityAnnotationDBO(hasBody = LocalizedStrings(nb = "Denne teksten sier noe om relevansen. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras mattis consectetur purus sit amet fermentum. Cum sociis natoque penatibus et magnis dis parturient montes."))),
    completeness=(QualityAnnotationDBO(hasBody = LocalizedStrings(nb = "Denne teksten sier noe om komplettheten. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras mattis consectetur purus sit amet fermentum."))),
    accuracy=(QualityAnnotationDBO(hasBody = LocalizedStrings(nb = "Denne teksten sier noe om nøyaktigheten. Cras mattis consectetur purus sit."))),
    availability=(QualityAnnotationDBO(hasBody = LocalizedStrings(nb = "Denne teksten sier noe om tilgjengeligheten. Vestibulum id ligula porta felis euismod semper. Duis mollis, est non commodo luctus, nisi erat porttitor ligula, eget lacinia odio sem nec elit. Cras mattis consectetur purus sit amet fermentum."))),
    language=listOf(
        "http://publications.europa.eu/resource/authority/language/NOR",
        "http://publications.europa.eu/resource/authority/language/ENG"
    ),
    landingPage=listOf("http://testetaten.no/landingsside/nr1", "www.this.can.happen/also"),
    euDataTheme = THEMES,
    losTheme = setOf("https://psi.norge.no/los/tema/lov-og-rett"),
    mobilityTheme = setOf("https://w3id.org/mobilitydcat-ap/mobility-theme/static-traffic-signs-and-regulations"),
    references= listOf(ReferenceDBO("references", "http://mycatalog/${CATALOG_ID}/datasets/1")),
    relatedResources =listOf(
        UriWithLabel("http://uri-1", LocalizedStrings(nb = "label-1-nb", en = "label-1-en")),
        UriWithLabel("http://uri-2", LocalizedStrings(nb = "label-2-nb", en = "label-2-en"))
    ),
    legalBasisForRestriction= listOf(
        UriWithLabel("https://lovdata.no/dokument/NL/lov/1992-12-04-126", LocalizedStrings(nb = "Lov om arkiv [arkivlova]")),
        UriWithLabel("http://lovdata/paragraph/20", LocalizedStrings(nb = "Den spesifikke loven § 20")),
        UriWithLabel(prefLabel = LocalizedStrings(nb = "Den mindre spesifikke loven, som ikke har tilhørende uri"))
    ),
    legalBasisForProcessing= listOf(UriWithLabel("http://lovdata/paragraph/2", LocalizedStrings(nb = "Den andre loven med lenger tittel § 2"))),
    legalBasisForAccess= listOf(UriWithLabel("http://lovdata/paragraph/10", LocalizedStrings(nb = "Den siste loven med den lengste tittelen § 10"))),
    distribution=listOf(DISTRIBUTION),
    sample = listOf(SAMPLE)
)

val TEST_CATALOG_1 = CatalogCount(
    id = CATALOG_ID,
    datasetCount = 0,
)


