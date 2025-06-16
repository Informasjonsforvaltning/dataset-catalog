package no.fdk.dataset_catalog.rdf

import no.fdk.dataset_catalog.model.*
import no.fdk.dataset_catalog.utils.isValidURI
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.rdf.model.*
import org.apache.jena.riot.Lang
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.util.URIref
import org.apache.jena.vocabulary.*
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import java.io.StringWriter
import java.lang.Exception
import java.net.URI
import java.net.URL
import java.time.LocalDate


// -------- Helper functions --------


fun Resource.safeAddLinkListProperty(property: Property, value: List<String>?): Resource {
    value?.forEach{ safeAddLinkedProperty(property, it) }
    return this
}

fun Resource.safeAddStringListLiteral(property: Property, value: List<String>?): Resource {
    value?.forEach{ safeAddStringLiteral(property, it) }
    return this
}

fun Resource.safeAddLangListProperty(property: Property, langMapList: List<Map<String, String>>?): Resource {
    langMapList?.forEach { safeAddPropertyByLang(property, it) }
    return this
}

fun Resource.safeAddLangListLiteral(property: Property, langMapList: List<Map<String, String>>?): Resource {
    langMapList?.forEach { safeAddLiteralByLang(property, it) }
    return this
}


fun Resource.addPublisherName(publisher: Publisher): Resource {
    if (publisher.name != null && publisher.prefLabel == null){
        safeAddProperty(FOAF.name, publisher.name)
    } else {
        if (publisher.prefLabel?.get("no") != null) safeAddLangLiteral(FOAF.name, publisher.prefLabel["no"], "no")
        if (publisher.prefLabel?.get("nb") != null) safeAddLangLiteral(FOAF.name, publisher.prefLabel["nb"], "nb")
        if (publisher.prefLabel?.get("nn") != null) safeAddLangLiteral(FOAF.name, publisher.prefLabel["nn"], "nn")
        if (publisher.prefLabel?.get("en") != null) safeAddLangLiteral(FOAF.name, publisher.prefLabel["en"], "en")
    }
    return this
}

fun Resource.safeAddPropertyByLang(property: Property, langMap: Map<String, String>?): Resource {
    if (langMap?.get("no") != null) addProperty(property, langMap["no"], "no")
    if (langMap?.get("nb") != null) addProperty(property, langMap["nb"], "nb")
    if (langMap?.get("nn") != null) addProperty(property, langMap["nn"], "nn")
    if (langMap?.get("en") != null) addProperty(property, langMap["en"], "en")
    return this
}

fun Resource.safeAddLiteralByLang(property: Property, langMap: Map<String, String>?): Resource {
    if (langMap?.get("no") != null) safeAddLangLiteral(property, langMap["no"], "no")
    if (langMap?.get("nb") != null) safeAddLangLiteral(property, langMap["nb"], "nb")
    if (langMap?.get("nn") != null) safeAddLangLiteral(property, langMap["nn"], "nn")
    if (langMap?.get("en") != null) safeAddLangLiteral(property, langMap["en"], "en")
    return this
}

fun Resource.safeAddStringLiteral(property: Property, value: String?): Resource =
    if (value.isNullOrEmpty()) this
    else addLiteral(property, value)

fun Resource.safeAddLiteral(property: Property, value: Literal?): Resource =
    if (value == null) this
    else addLiteral(property, value)

fun Resource.safeAddLangLiteral(property: Property, value: String?, lang: String): Resource =
    if (value.isNullOrEmpty()) this
    else {
        val literal = model.createLiteral(value, lang)
        addLiteral(property, literal)
    }

fun Resource.safeAddProperty(property: Property, value: String?): Resource =
    if (value.isNullOrEmpty()) this
    else addProperty(property, value)

fun Resource.safeAddProperty(property: Property, value: Resource?): Resource =
    if (value == null) this
    else addProperty(property, value)

fun Resource.safeAddResourceProperty(property: Property, value: Resource?): Resource =
    if (value == null) this
    else addProperty(property, value)

fun Resource.safeAddDateTimeLiteral(property: Property, dateTime: LocalDate?): Resource =
    if ( dateTime != null ) {
        safeAddLiteral(property, model.createTypedLiteral(dateTime.toString(), XSDDatatype.XSDdate))
    } else this

fun Resource.safeAddLinkedProperty(property: Property, value: String?): Resource =
    if (value.isNullOrEmpty()) this
    else addProperty(property, model.createResource(value))

fun Resource.safeAddURLs(property: Property, value: List<String?>?): Resource {
    value?.forEach {
        if (it.isValidURL()) {
            safeAddProperty(property, model.safeCreateLinkedResource(it))
        }
    }
    return this
}


fun String.addContactStringPrefix(prefix: String): String? =
    when {
        this.startsWith(prefix) -> this.trim()
        this.isNotEmpty() -> prefix + this.trim()
        else -> null
    }

// -------- Blank Nodes --------

fun Resource.addContactPoints(contactPoints: Collection<Contact>?): Resource {
    contactPoints?.forEach {
        addProperty(DCAT.contactPoint,
            model.safeCreateResource(it.uri)
                .addProperty(RDF.type, VCARD4.Organization)
                .safeAddStringLiteral(VCARD4.fn, it.fullname)
                .safeAddURLs(VCARD4.hasURL, listOf(it.hasURL))
                .safeAddStringLiteral(VCARD4.organization_name, it.organizationName)
                .safeAddStringLiteral(VCARD4.organization_unit, it.organizationUnit)
                .safeAddLinkedProperty(VCARD4.hasEmail, it.email?.addContactStringPrefix("mailto:"))
                .safeAddLinkedProperty(VCARD4.hasTelephone, it.hasTelephone?.addContactStringPrefix("tel:"))
        )
    }
    return this
}

fun Resource.addConformsTo(conformsTo: Collection<SkosConcept>?): Resource {
    conformsTo?.forEach {
        if (!it.uri.isNullOrEmpty() || it.prefLabel.isValidLangField()) {
            addProperty(DCTerms.conformsTo,
                model.safeCreateResource(it.uri)
                    .addProperty(RDF.type, DCTerms.Standard)
                    .safeAddLinkedProperty(RDFS.seeAlso, it.uri)
                    .safeAddLiteralByLang(DCTerms.title, it.prefLabel)
            )
        }
    }
    return this
}

fun Resource.addConformsToFromListOfUris(conformsTo: Collection<String>?): Resource {
    conformsTo?.forEach {
        addProperty(DCTerms.conformsTo,
            model.safeCreateResource(it)
                .addProperty(RDF.type, DCTerms.Standard)
                .safeAddLinkedProperty(DCTerms.source, it)
            )
        }
    return this
}

fun Resource.addDistribution(property: Property, distributions: Collection<Distribution>?): Resource {
    distributions?.forEach {
        if (it.hasNonNullOrEmptyProperty()) {
            addProperty(property,
                model.safeCreateResource(it.uri)
                    .addProperty(RDF.type, DCAT.Distribution)
                    .safeAddStringLiteral(DCTerms.identifier, it.id)
                    .safeAddLiteralByLang(DCTerms.title, it.title)
                    .safeAddLiteralByLang(DCTerms.description, it.description)
                    .safeAddURLs(DCAT.accessURL, it.accessURL)
                    .safeAddURLs(DCAT.downloadURL, it.downloadURL)
                    .safeAddURLs(DCTerms.license, listOfNotNull(it.license?.uri))
                    .addConformsTo(it.conformsTo)
                    .safeAddURLs(FOAF.page, it.page?.map { page -> page.uri })
                    .safeAddURLs(DCTerms.format, it.format)
                    .safeAddURLs(DCAT.mediaType, it.mediaType)
                    .addDataDistributionServices(it.accessService)
                    .addDataDistributionServices(it.accessServiceUris)
            )
        }
    }
    return this
}

private fun Distribution.hasNonNullOrEmptyProperty(): Boolean =
    title?.all { entry -> entry.value.isNullOrEmpty() } == false ||

    description?.all { entry -> entry.value.isNullOrEmpty() } == false ||

    accessURL?.all { entry -> entry.isNullOrEmpty() } == false ||

    !license?.uri.isNullOrEmpty() ||

    conformsTo?.all { entry -> entry.uri.isNullOrEmpty() } == false ||

    page?.all { entry -> entry.uri.isNullOrEmpty() } == false ||

    format?.all { entry -> entry.isNullOrEmpty() } == false ||

    mediaType?.all { entry -> entry.isNullOrEmpty() } == false ||

    !accessService.isNullOrEmpty()

// TODO: add dcat:endpointURLs and make sure front-end sends necessary data (https://doc.difi.no/review/dcat-ap-no/#_obligatoriske_egenskaper_for_datatjeneste)


fun Resource.addThemes(ds: Dataset): Resource {
    val uniqueThemes = mutableSetOf<String>()

    ds.theme?.mapNotNull { it.uri }
        ?.filter { it.isValidURI() }
        ?.let { uniqueThemes.addAll(it) }

    ds.losTheme?.filter { it.isValidURI() }
        ?.let { uniqueThemes.addAll(it) }

    ds.euDataTheme?.filter { it.isValidURI() }
        ?.let { uniqueThemes.addAll(it) }
    safeAddLinkListProperty(DCAT.theme, uniqueThemes.toList())

    return this
}

fun Resource.addDataDistributionServices(dataDistributionServices: Collection<DataDistributionService>?): Resource {
    dataDistributionServices?.forEach {
        val accessServiceResource = model.safeCreateResource(it.uri)
        if (accessServiceResource.isURIResource) {
            addProperty(DCAT.accessService, accessServiceResource)
        }
    }
    return this
}

fun Resource.addDataDistributionServices(uris: Set<String>?): Resource {
    uris?.forEach {
        val accessServiceResource = model.safeCreateResource(it)
        if (accessServiceResource.isURIResource) {
            addProperty(DCAT.accessService, accessServiceResource)
        }
    }
    return this
}

fun Resource.addCPSVNORules(ds: Dataset): Resource {
    ds.legalBasisForAccess?.forEach { addRule(it, CPSVNO.ruleForDisclosure) }
    ds.legalBasisForProcessing?.forEach { addRule(it, CPSVNO.ruleForDataProcessing) }
    ds.legalBasisForRestriction?.forEach { addRule(it, CPSVNO.ruleForNonDisclosure) }

    return this
}

private fun Resource.addRule(rule: SkosConcept, ruleType: Resource): Resource {
    if (rule.uri.isValidURL() && !rule.prefLabel.isNullOrEmpty()) {
        addProperty(
            CPSV.follows,
            model.createResource()
                .addProperty(RDF.type, CPSV.Rule)
                .addProperty(DCTerms.type, ruleType)
                .addProperty(
                    CPSV.implements,
                    model.createResource()
                        .addProperty(RDF.type, ELI.LegalResource)
                        .safeAddLinkedProperty(RDFS.seeAlso, rule.uri)
                        .addProperty(
                            DCTerms.type,
                            model.createResource()
                                .addProperty(RDF.type, SKOS.Concept)
                                .safeAddLiteralByLang(SKOS.prefLabel, rule.prefLabel)
                        )
                )
        )
    }

    return this
}

fun Resource.addTemporal(temporal: List<PeriodOfTime>?): Resource {
    temporal?.forEach {
        if (it.startDate != null || it.endDate != null) {
            addProperty(DCTerms.temporal,
                model.safeCreateResource()
                    .addProperty(RDF.type, DCTerms.PeriodOfTime)
                    .safeAddDateTimeLiteral(Schema.startDate, it.startDate)
                    .safeAddDateTimeLiteral(Schema.endDate, it.endDate))
        }
    }
    return this
}

fun Resource.addQualityAnnotation(body: Map<String, String>?, dimension: Resource): Resource {
    if (body?.isNotEmpty() == true) {
        addProperty(DQV.hasQualityAnnotation,
            model.safeCreateResource()
                .addProperty(RDF.type, DQV.QualityAnnotation)
                .safeAddLinkedProperty(DQV.inDimension, dimension.uri)
                .addQualityAnnotationBody(body))
    }
    return this
}

fun Resource.addQualityAnnotationBody(body: Map<String, String>?): Resource {
    body?.forEach {(key, value) ->
        addProperty(OA.hasBody,
            model.safeCreateResource()
                .addProperty(RDF.type, OA.TextualBody)
                .safeAddStringLiteral(RDF.value, value)
                .safeAddLinkedProperty(DCTerms.language, keywordToLinguisticSystem(key).uri)
                .safeAddLinkedProperty(DCTerms.format, "http://publications.europa.eu/resource/authority/file-type/TXT")
        )
    }
    return this
}

private fun getReferencePropertyURI(code: String?, uri: String?): String {
    return if (!code.isNullOrEmpty()) {
        val newCode = code.replaceFirst("dct:", "")
        if (newCode.startsWith(DCTerms.NS)) {
            newCode
        } else {
            DCTerms.getURI() + newCode
        }
    } else uri!!
}

fun Resource.addReferences(references: Collection<Reference>?): Resource {
    references?.forEach {
        if (it.isValidReference()) {

            val referencePropertyURI: String = getReferencePropertyURI(
                it.referenceType?.code,
                it.referenceType?.uri)

            safeAddResourceProperty(
                model.createProperty(referencePropertyURI),
                model.safeCreateResource(it.source?.uri)
            )
        }
    }
    return this
}

private fun Reference.isValidReference(): Boolean =
    referenceType != null &&
    source != null &&
    (!referenceType.uri.isNullOrEmpty() || !referenceType.code.isNullOrEmpty()) &&
    !source.uri.isNullOrEmpty()

fun Resource.addRelations(relations: Collection<SkosConcept>?): Resource {
    relations?.forEach {
        if (it.isValidRelation()) {
            addProperty(
                DCTerms.relation,
                model.safeCreateResource(it.uri)
                    .addProperty(RDF.type, RDFS.Resource)
                    .safeAddLiteralByLang(RDFS.label, it.prefLabel)
            )
        }
    }
    return this
}

private fun SkosConcept.isValidRelation(): Boolean =
    !uri.isNullOrEmpty() || prefLabel.isValidLangField()

private fun Map<String, String>?.isValidLangField(): Boolean =
    when {
        isNullOrEmpty() -> false
        getOrDefault("nb", "").isNotBlank() -> true
        getOrDefault("nn", "").isNotBlank() -> true
        getOrDefault("en", "").isNotBlank() -> true
        getOrDefault("no", "").isNotBlank() -> true
        else -> false
    }

fun Resource.addQualifiedAttributions(qualifiedAttributions: Collection<String>?): Resource {
    qualifiedAttributions?.forEach {
        addProperty(PROV.qualifiedAttribution,
            model.safeCreateResource()
                .addProperty(RDF.type, PROV.Attribution)
                .safeAddResourceProperty(DCAT.hadRole, ResourceFactory.createResource(URIref.encode("http://registry.it.csiro.au/def/isotc211/CI_RoleCode/contributor")))
                .safeAddResourceProperty(PROV.agent, ResourceFactory.createResource(URIref.encode("https://data.brreg.no/enhetsregisteret/api/enheter/$it"))))
    }
    return this
}

fun Resource.addPublisher(publisher: Publisher?): Resource {
    publisher?.let {
        if (it.uri != null) addProperty(DCTerms.publisher, model.safeCreateResource(it.uri))
        else {
            addProperty(
                DCTerms.publisher,
                model.createResource()
                    .addProperty(RDF.type, FOAF.Agent)
                    .addPublisherName(it)
                    .safeAddStringLiteral(DCTerms.identifier, it.id)
            )
        }
    }
    return this
}


fun Resource.addSubjects(subjects: Collection<Concept>?): Resource {
    subjects?.forEach {
        addProperty(DCTerms.subject,
            model.safeCreateResource(it.uri)
                .addProperty(RDF.type, SKOS.Concept)
                .safeAddProperty(DCTerms.identifier, it.identifier)
                .safeAddLangListLiteral(SKOS.altLabel, it.altLabel)
                .safeAddPropertyByLang(SKOS.prefLabel, it.prefLabel)
                .safeAddLiteralByLang(SKOS.definition, it.definition?.text)
        )
    }
    return this
}

fun Resource.addInSeries(inSeries: String?, inSeriesIsPublished: Boolean): Resource =
    if (inSeriesIsPublished) safeAddLinkedProperty(ResourceFactory.createProperty("${DCAT.getURI()}inSeries"), inSeries)
    else this

fun Resource.addLanguages(language: Collection<SkosCode>?): Resource {
    language?.forEach {
        addProperty(DCTerms.language,
            model.safeCreateResource(it.uri)
                .addProperty(RDF.type, DCTerms.LinguisticSystem)
                .safeAddLiteralByLang(SKOS.prefLabel, it.prefLabel)
                .safeAddStringLiteral(AT.authorityCode, it.code))
    }
    return this
}

fun jenaLangFromAcceptHeader(accept: String?): Lang =
    when {
        accept == null -> throw HttpServerErrorException(HttpStatus.NOT_ACCEPTABLE)
        accept.contains(Lang.TURTLE.headerString) -> Lang.TURTLE
        accept.contains(Lang.RDFXML.headerString) -> Lang.RDFXML
        accept.contains(Lang.RDFJSON.headerString) -> Lang.RDFJSON
        accept.contains(Lang.JSONLD.headerString) -> Lang.JSONLD
        accept.contains(Lang.NTRIPLES.headerString) -> Lang.NTRIPLES
        accept.contains(Lang.NQUADS.headerString) -> Lang.NQUADS
        accept.contains(Lang.TRIG.headerString) -> Lang.TRIG
        accept.contains(Lang.TRIX.headerString) -> Lang.TRIX
        accept.contains("text/n3") -> Lang.N3
        else -> throw HttpServerErrorException(HttpStatus.NOT_ACCEPTABLE)
    }

fun Resource.addDatasetType(datasetType: String?): Resource {
    if (datasetType.isValidURL()) {
        safeAddProperty(DCTerms.type, model.safeCreateLinkedResource(datasetType))
    } else {
        safeAddProperty(DCTerms.type, datasetType)
    }
    return this
}

// -------- Model Extensions --------

fun Model.createRDFResponse(lang: Lang): String =
    StringWriter().use{ out ->
        write(out, lang.name)
        out.toString()
    }

fun Model.safeCreateResource(value: String? = null): Resource =
    try {
        value
            ?.let(::URI)
            ?.takeIf { it.isAbsolute && !it.isOpaque && !it.host.isNullOrEmpty() }
            ?.let { createResource(value) }
            ?: createResource()
    } catch (e: Exception) {
        createResource()
    }

fun Model.safeCreateLinkedResource(value: String? = null) : Resource? =
    if (!value.isNullOrEmpty()) {
        createResource(value)
    } else null

// -------- Utils --------

enum class JenaType(val value: String){
    TURTLE("TURTLE"),
    NOT_ACCEPTABLE("")
}

fun String?.isValidURL(): Boolean =
    try {
        URL(this)
        true
    } catch (e: Exception) {
        false
    }

enum class LinguisticSystem(val uri: String) {
    NOR("http://publications.europa.eu/resource/authority/language/NOR"),
    NOB("http://publications.europa.eu/resource/authority/language/NOB"),
    NNO("http://publications.europa.eu/resource/authority/language/NNO"),
    ENG("http://publications.europa.eu/resource/authority/language/ENG")
}

fun keywordToLinguisticSystem(keyword: String): LinguisticSystem =
    when(keyword) {
        "en" -> LinguisticSystem.ENG
        "nb" -> LinguisticSystem.NOB
        "nn" -> LinguisticSystem.NNO
        else -> LinguisticSystem.NOR
    }

fun Dataset.dctIdentifier(): List<String>? =
    originalUri?.let { listOf(it) }
        ?: uri?.let { listOf(it) }

class SeriesData(
    val inSeries: String?,
    val next: String?,
    val prev: String?,
    val first: String?,
    val last: String?
)
