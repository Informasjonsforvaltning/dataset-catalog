package no.fdk.dataset_catalog.rdf

import no.fdk.dataset_catalog.model.*
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.rdf.model.*
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.util.URIref
import org.apache.jena.vocabulary.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.net.URI
import java.net.URL
import java.time.LocalDate


// -------- Helper functions --------


fun Resource.safeAddLinkListProperty(property: Property, value: List<String>?): Resource {
    value?.forEach{ safeAddLinkedProperty(property, it) }
    return this
}

fun Resource.safeAddStringListProperty(property: Property, value: List<String>?): Resource {
    value?.forEach{ safeAddProperty(property, it) }
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

fun Resource.addSkosConcepts(property: Property, skosConcept: Collection<SkosConcept>?, resource: Resource): Resource {
    skosConcept?.forEach {
        if (!it.uri.isNullOrEmpty()) {
            addProperty(property,
                model.safeCreateResource()
                    .addProperty(RDF.type, resource)
                    .safeAddLinkedProperty(RDF.type, it.extraType)
                    .addProperty(RDF.type, SKOS.Concept)
                    .safeAddProperty(DCTerms.source, it.uri)
                    .safeAddLiteralByLang(SKOS.prefLabel, it.prefLabel)
            )
        }
    }
    return this
}

fun Resource.addDistribution(property: Property, distributions: Collection<Distribution>?, baseURI: String): Resource {
    distributions?.forEach {
        if (it.hasNonNullProperty()) {
            addProperty(property,
                model.safeCreateResource(it.uri)
                    .addProperty(RDF.type, DCAT.Distribution)
                    .safeAddStringLiteral(DCTerms.identifier, it.id)
                    .safeAddLiteralByLang(DCTerms.title, it.title)
                    .safeAddLiteralByLang(DCTerms.description, it.description)
                    .safeAddURLs(DCAT.accessURL, it.accessURL)
                    .safeAddURLs(DCAT.downloadURL, it.downloadURL)
                    .addSkosConcepts(DCTerms.license, listOfNotNull(it.license), DCTerms.LicenseDocument)
                    .addSkosConcepts(DCTerms.conformsTo, it.conformsTo, DCTerms.Standard)
                    .addSkosConcepts(FOAF.page, it.page, FOAF.Document)
                    .safeAddStringListLiteral(DCTerms.format, it.format)
                    .addDataDistributionServices(it.accessService, baseURI)
            )
        }
    }
    return this
}

private fun Distribution.hasNonNullProperty(): Boolean =
    title.isNullOrEmpty() ||
    description.isNullOrEmpty() ||
    (!accessURL.isNullOrEmpty()) ||
    (license != null) ||
    (!conformsTo.isNullOrEmpty()) ||
    (!page.isNullOrEmpty()) ||
    (!format.isNullOrEmpty()) ||
    (!accessService.isNullOrEmpty())

// TODO: add dcat:endpointURLs and make sure front-end sends necessary data (https://doc.difi.no/review/dcat-ap-no/#_obligatoriske_egenskaper_for_datatjeneste)
// TODO: add a list of dct:MediaTypes (https://doc.difi.no/review/dcat-ap-no/#distribusjon-medietype)

fun Resource.addDataDistributionServices(dataDistributionServices: Collection<DataDistributionService>?, baseURI: String): Resource {
    dataDistributionServices?.forEach {
        val uri = when {
            !it.uri.isNullOrEmpty() -> it.uri
            !it.id.isNullOrEmpty() -> "$baseURI/accessService/${it.id}"
            else -> null
        }
        addProperty(DCATapi.accessService,
            model.safeCreateResource(uri)
                .addProperty(RDF.type, DCATapi.DataDistributionService)
                .safeAddStringLiteral(DCTerms.identifier, it.id)
                .safeAddLiteralByLang(DCTerms.title, it.title)
                .safeAddLiteralByLang(DCTerms.description, it.description)
                .addPublisher(it.publisher)
                .addSkosConcepts(DCATapi.endpointDescription, it.endpointDescription, FOAF.Document)
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


fun Resource.addQualityAnnotation(qualityAnnotation: QualityAnnotation?): Resource {
    qualityAnnotation?.let {
        addProperty(DQV.hasQualityAnnotation,
            model.safeCreateResource()
                .addProperty(RDF.type, DQV.QualityAnnotation)
                .addQualityAnnotationDimension(DQV.inDimension, it.inDimension)
                .addQualityAnnotationBody(it.hasBody))
    }
    return this
}

fun Resource.addQualityAnnotationDimension(property: Property, dimension: String?): Resource {
    DQV.resolveDimensionResource(dimension)
        ?.let {
            safeAddLinkedProperty(property, dimension)
        }
    return this
}

fun Resource.addQualityAnnotationBody(body: Map<String, String>?): Resource {
    body?.let {
        addProperty(PROV.hasBody,
            model.safeCreateResource()
                .safeAddLiteralByLang(RDF.value, body))
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

            val referenceProperty = model.createProperty(referencePropertyURI)

            if (it.source?.prefLabel.isNullOrEmpty()) {
                safeAddResourceProperty(referenceProperty,
                    model.safeCreateResource(it.source?.uri))
            } else {
                addProperty(referenceProperty,
                    model.safeCreateResource()
                        .addProperty(RDF.type, DCAT.Dataset)
                        .safeAddLiteralByLang(SKOS.prefLabel, it.source?.prefLabel)
                        .safeAddResourceProperty(DCTerms.source, model.safeCreateResource(it.source?.uri)))
            }
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
        addProperty(DCTerms.relation,
            model.safeCreateResource(it.uri)
                .addProperty(RDF.type, RDFS.Resource)
                .safeAddLiteralByLang(RDFS.label, it.prefLabel)
        )
    }
    return this
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

private fun String?.getIfNotNullOrEmpty(): String? =
    if (this.isNullOrEmpty())
        null
    else
        this

fun Resource.addPublisher(publisher: Publisher?): Resource {
    publisher?.let {
        addProperty(DCTerms.publisher,
            model.safeCreateResource(it.uri.getIfNotNullOrEmpty() ?: it.id.getIfNotNullOrEmpty())
                .addProperty(RDF.type, FOAF.Agent)
                .addPublisherName(it)
                .safeAddLiteralByLang(SKOS.prefLabel, it.prefLabel)
                .safeAddStringLiteral(DCTerms.identifier, it.id))
    }
    return this
}


fun Resource.safeAddSubjectCreatorId(property: Property, uri: String?): Resource =
    if (uri !=null) {
        this.safeAddLinkedProperty(property, uri)
    } else this


fun Resource.addSubjects(subjects: Collection<Subject>?): Resource {
    subjects?.forEach {
        addProperty(DCTerms.subject,
            model.safeCreateResource(it.uri)
                .addProperty(RDF.type, SKOS.Concept)
                .safeAddProperty(DCTerms.identifier, it.identifier)
                .safeAddSubjectCreatorId(DCTerms.creator, it.creator?.id)
                .safeAddProperty(DCTerms.source, it.source)
                .safeAddLangListLiteral(SKOS.altLabel, it.altLabel)
                .safeAddStringListLiteral(SKOS.inScheme, it.inScheme)
                .safeAddPropertyByLang(SKOS.prefLabel, it.prefLabel)
                .safeAddPropertyByLang(SKOS.note, it.note)
                .safeAddLiteralByLang(SKOS.definition, it.definition)
        )
    }
    return this
}

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


// -------- Model Extensions --------

fun Model.createRDFResponse(): String =
    ByteArrayOutputStream().use{ out ->
        write(out, "TURTLE")
        out.flush()
        out.toString("UTF-8")
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

fun acceptHeaderToJenaType(accept: String?): JenaType =
    when (accept) {
        "text/turtle" -> JenaType.TURTLE
        else -> JenaType.NOT_ACCEPTABLE
    }

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