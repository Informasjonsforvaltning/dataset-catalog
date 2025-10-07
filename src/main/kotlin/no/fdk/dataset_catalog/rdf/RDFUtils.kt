package no.fdk.dataset_catalog.rdf

import no.fdk.dataset_catalog.model.*
import no.fdk.dataset_catalog.utils.defaultLogger
import no.fdk.dataset_catalog.utils.isValidURI
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.rdf.model.*
import org.apache.jena.riot.Lang
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
    value?.forEach { safeAddLinkedProperty(property, it) }
    return this
}

fun Resource.safeAddLocalizedString(property: Property, langMap: LocalizedStrings?): Resource {
    langMap?.nb?.let { safeAddLangLiteral(property, it, "nb") }
    langMap?.nn?.let { safeAddLangLiteral(property, it, "nn") }
    langMap?.en?.let { safeAddLangLiteral(property, it, "en") }
    return this
}

fun Resource.safeAddLocalizedStringList(property: Property, langMap: LocalizedStringLists?): Resource {
    langMap?.nb?.forEach { safeAddLangLiteral(property, it, "nb") }
    langMap?.nn?.forEach { safeAddLangLiteral(property, it, "nn") }
    langMap?.en?.forEach { safeAddLangLiteral(property, it, "en") }
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
    if (dateTime != null) {
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

fun Resource.addContactPoints(contactPoints: List<ContactPoint>?): Resource {
    contactPoints?.forEach {
        val resource = model.safeCreateResource()
            .addProperty(RDF.type, VCARD4.Organization)
            .safeAddLocalizedString(VCARD4.fn, it.name)
            .safeAddURLs(VCARD4.hasURL, listOf(it.url))
            .safeAddLinkedProperty(VCARD4.hasEmail, it.email?.addContactStringPrefix("mailto:"))
        if (!it.phone.isNullOrBlank()) {
            resource.addProperty(VCARD4.hasTelephone, model.telephoneResource(it.phone))
        }
        addProperty(DCAT.contactPoint, resource)
    }
    return this
}

fun Resource.addConformsTo(conformsTo: Collection<UriWithLabel>?): Resource {
    conformsTo?.forEach {
        if (!it.uri.isNullOrEmpty() || it.prefLabel.isValidLangField()) {
            addProperty(
                DCTerms.conformsTo,
                model.safeCreateResource()
                    .addProperty(RDF.type, DCTerms.Standard)
                    .safeAddLinkedProperty(RDFS.seeAlso, it.uri)
                    .safeAddLocalizedString(DCTerms.title, it.prefLabel)
            )
        }
    }
    return this
}


fun Resource.addConformsToFromListOfUris(conformsTo: Collection<String>?): Resource {
    conformsTo?.forEach {
        addProperty(
            DCTerms.conformsTo,
            model.safeCreateResource()
                .addProperty(RDF.type, DCTerms.Standard)
                .safeAddLinkedProperty(RDFS.seeAlso, it)
        )
    }
    return this
}

fun Resource.addDatasetDistribution(property: Property, distributions: Collection<DistributionDBO>?): Resource {
    distributions?.forEach {
        if (it.hasNonNullOrEmptyProperty()) {
            addProperty(
                property,
                model.safeCreateResource()
                    .addProperty(RDF.type, DCAT.Distribution)
                    .safeAddLocalizedString(DCTerms.title, it.title)
                    .safeAddLocalizedString(DCTerms.description, it.description)
                    .safeAddURLs(DCAT.accessURL, it.accessURL)
                    .safeAddURLs(DCAT.downloadURL, it.downloadURL)
                    .safeAddURLs(DCTerms.license, listOfNotNull(it.license))
                    .addConformsTo(it.conformsTo)
                    .safeAddURLs(DCTerms.format, it.format)
                    .safeAddURLs(DCAT.mediaType, it.mediaType)
                    .addDistributionServices(it.accessServices)
            )
        }
    }
    return this
}


private fun DistributionDBO.hasNonNullOrEmptyProperty(): Boolean =
    title?.run { listOf(nb, nn, en).any { !it.isNullOrEmpty() } } == true ||
        description?.run { listOf(nb, nn, en).any { !it.isNullOrEmpty() } } == true ||
        accessURL?.any { it.isNotEmpty() } == true ||
        !license.isNullOrEmpty() ||
        conformsTo?.any { !it.uri.isNullOrEmpty() } == true ||
        format?.any { it.isNotEmpty() } == true ||
        mediaType?.any { it.isNotEmpty() } == true ||
        !accessServices.isNullOrEmpty()

fun Resource.addDatasetThemes(ds: DatasetDBO): Resource {
    val uniqueThemes = mutableSetOf<String>()

    ds.losTheme?.filter { it.isValidURI() }
        ?.let { uniqueThemes.addAll(it) }

    ds.euDataTheme?.filter { it.isValidURI() }
        ?.let { uniqueThemes.addAll(it) }
    safeAddLinkListProperty(DCAT.theme, uniqueThemes.toList())

    return this
}

fun Resource.addDistributionServices(
    accessServices: Set<String>?,
): Resource {
    accessServices?.forEach {
        val accessServiceResource = model.safeCreateResource(it)
        if (accessServiceResource.isURIResource) {
            addProperty(DCAT.accessService, accessServiceResource)
        }
    }
    return this
}

fun Resource.addLegalBasis(ds: DatasetDBO): Resource {
    ds.legalBasisForAccess?.forEach { addRule(it, CPSVNO.ruleForDisclosure) }
    ds.legalBasisForProcessing?.forEach { addRule(it, CPSVNO.ruleForDataProcessing) }
    ds.legalBasisForRestriction?.forEach { addRule(it, CPSVNO.ruleForNonDisclosure) }

    return this
}

private fun Resource.addRule(rule: UriWithLabel, ruleType: Resource): Resource {
    if (rule.uri.isValidURL() || rule.prefLabel.isValidLangField()) {
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
                                .safeAddLocalizedString(SKOS.prefLabel, rule.prefLabel)
                        )
                )
        )
    }
    return this
}

fun Resource.addTemporal(temporal: List<PeriodOfTimeDBO>?): Resource {
    temporal?.forEach {
        if (it.startDate != null || it.endDate != null) {
            addProperty(
                DCTerms.temporal,
                model.safeCreateResource()
                    .addProperty(RDF.type, DCTerms.PeriodOfTime)
                    .safeAddDateTimeLiteral(Schema.startDate, it.startDate)
                    .safeAddDateTimeLiteral(Schema.endDate, it.endDate)
            )
        }
    }
    return this
}

fun Resource.addQualityAnnotation(qualityAnnotation: QualityAnnotationDBO?, dimension: Resource): Resource {
    qualityAnnotation?.let {
        addProperty(
            DQV.hasQualityAnnotation,
            model.safeCreateResource()
                .addProperty(RDF.type, DQV.QualityAnnotation)
                .safeAddLinkedProperty(DQV.inDimension, dimension.uri)
                .addQualityAnnotationBody(it.hasBody)
        )
    }
    return this
}

fun Resource.addQualityAnnotationBody(body: LocalizedStrings?): Resource {
    body?.nb?.let { nb ->
        addQualityAnnotationBody(nb, LinguisticSystem.NOB.uri)
    }
    body?.nn?.let { nn ->
        addQualityAnnotationBody(nn, LinguisticSystem.NNO.uri)
    }
    body?.en?.let { en ->
        addQualityAnnotationBody(en, LinguisticSystem.ENG.uri)
    }
    return this
}

fun Resource.addQualityAnnotationBody(text: String, lang: String) {
    addProperty(
        OA.hasBody,
        model.safeCreateResource()
            .addProperty(RDF.type, OA.TextualBody)
            .safeAddStringLiteral(RDF.value, text)
            .safeAddLinkedProperty(DCTerms.language, lang)
            .safeAddLinkedProperty(DCTerms.format, "http://publications.europa.eu/resource/authority/file-type/TXT")
    )
}

private fun referenceTypeToProperty(referenceTypeString: String?): Property? {
    return when (referenceTypeString) {
        "hasVersion" -> DCTerms.hasVersion
        "isVersionOf" -> DCTerms.isVersionOf
        "isPartOf" -> DCTerms.isPartOf
        "hasPart" -> DCTerms.hasPart
        "isReferencedBy" -> DCTerms.isReferencedBy
        "references" -> DCTerms.references
        "isReplacedBy" -> DCTerms.isReplacedBy
        "replaces" -> DCTerms.replaces
        "relation" -> DCTerms.relation
        "source" -> DCTerms.source
        else -> {
            defaultLogger.warn("Unknown reference type $referenceTypeString")
            null
        }
    }
}

fun Resource.addReferences(references: Collection<ReferenceDBO>?): Resource {
    references?.forEach {
        if (it.isValidReference()) {
            referenceTypeToProperty(it.referenceType)?.let { referenceProperty ->
                safeAddResourceProperty(
                    referenceProperty,
                    model.safeCreateResource(it.source)
                )
            }
        }
    }
    return this
}

private fun ReferenceDBO.isValidReference(): Boolean =
    referenceType != null &&
        source != null &&
        (referenceType.isNotEmpty()) &&
        source.isNotEmpty()


fun Resource.addRelatedResources(relations: List<UriWithLabel>?): Resource {
    relations?.forEach {
        if (it.isValidRelation()) {
            addProperty(
                DCTerms.relation,
                model.safeCreateResource(it.uri)
                    .addProperty(RDF.type, RDFS.Resource)
                    .safeAddLocalizedString(RDFS.label, it.prefLabel)
            )
        }
    }
    return this
}

private fun UriWithLabel.isValidRelation(): Boolean =
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

private fun LocalizedStrings?.isValidLangField(): Boolean =
    when {
        this == null -> false
        !nb.isNullOrBlank() -> true
        !nn.isNullOrBlank() -> true
        !en.isNullOrBlank() -> true
        else -> false
    }

fun Resource.addQualifiedAttributions(qualifiedAttributions: Collection<String>?): Resource {
    qualifiedAttributions?.forEach {
        addProperty(
            PROV.qualifiedAttribution,
            model.safeCreateResource()
                .addProperty(RDF.type, PROV.Attribution)
                .safeAddResourceProperty(
                    DCAT.hadRole,
                    ResourceFactory.createResource(URIref.encode("http://registry.it.csiro.au/def/isotc211/CI_RoleCode/contributor"))
                )
                .safeAddResourceProperty(
                    PROV.agent,
                    ResourceFactory.createResource(URIref.encode("https://data.brreg.no/enhetsregisteret/api/enheter/$it"))
                )
        )
    }
    return this
}

fun Resource.addConcepts(subjects: Set<String>?): Resource {
    subjects?.forEach {
        addProperty(
            DCTerms.subject,
            model.safeCreateResource(it)
        )
    }
    return this
}

fun Resource.addLanguages(language: List<String>?): Resource {
    language?.forEach {
        addProperty(
            DCTerms.language,
            model.safeCreateResource(it)
        )
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

fun Model.telephoneResource(telephone: String): Resource =
    telephone.trim { it <= ' ' }
        .filterIndexed { index, c ->
            when {
                index == 0 && c == '+' -> true // global-number-digits
                c in '0'..'9' -> true // digit
                else -> false // skip visual-separator and other content
            }
        }
        .let { createResource("tel:$it") }

fun Model.createRDFResponse(lang: Lang): String =
    StringWriter().use { out ->
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

fun Model.safeCreateLinkedResource(value: String? = null): Resource? =
    if (!value.isNullOrEmpty()) {
        createResource(value)
    } else null

// -------- Utils --------

fun String?.isValidURL(): Boolean =
    try {
        URL(this)
        true
    } catch (e: Exception) {
        false
    }

enum class LinguisticSystem(val uri: String) {
    NOB("http://publications.europa.eu/resource/authority/language/NOB"),
    NNO("http://publications.europa.eu/resource/authority/language/NNO"),
    ENG("http://publications.europa.eu/resource/authority/language/ENG")
}

class SeriesData(
    val inSeries: String?,
    val next: String?,
    val prev: String?,
    val first: String?,
    val last: String?
)
