package no.fdk.dataset_catalog.rdf

import no.fdk.dataset_catalog.model.*
import org.apache.jena.rdf.model.*
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.util.URIref
import org.apache.jena.vocabulary.*
import org.apache.jena.vocabulary.OA
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.*


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
        safeAddLangLiteral(FOAF.name, publisher.name, "nb")
    } else {
        if (publisher.prefLabel?.get("nb") != null) safeAddLangLiteral(FOAF.name, publisher.prefLabel["nb"], "nb")
        if (publisher.prefLabel?.get("nn") != null) safeAddLangLiteral(FOAF.name, publisher.prefLabel["nn"], "nn")
        if (publisher.prefLabel?.get("en") != null) safeAddLangLiteral(FOAF.name, publisher.prefLabel["en"], "en")
    }
    return this
}

fun Resource.safeAddPropertyByLang(property: Property, langMap: Map<String, String>?): Resource {
    if (langMap?.get("nb") != null) addProperty(property, langMap["nb"], "nb")
    if (langMap?.get("nn") != null) addProperty(property, langMap["nn"], "nn")
    if (langMap?.get("en") != null) addProperty(property, langMap["en"], "en")
    return this
}

fun Resource.safeAddLiteralByLang(property: Property, langMap: Map<String, String>?): Resource {
    if (langMap?.get("nb") != null) safeAddLangLiteral(property, langMap["nb"], "nb")
    if (langMap?.get("nn") != null) safeAddLangLiteral(property, langMap["nn"], "nn")
    if (langMap?.get("en") != null) safeAddLangLiteral(property, langMap["en"], "en")
    return this
}

fun Resource.safeAddDatasetListLinkedProperty(property: Property, value: List<Dataset>?): Resource =
    if(value == null || value.isEmpty()) this
    else {
        value.map { addProperty(property, model.createResource(it.originalUri ?: it.uri)) }
        this
    }

fun Resource.safeAddStringLiteral(property: Property, value: String?): Resource =
    if(value == null) this
    else addLiteral(property, value)

fun Resource.safeAddLiteral(property: Property, value: Literal?): Resource =
    if(value == null) this
    else addLiteral(property, value)

fun Resource.safeAddLangLiteral(property: Property, value: String?, lang: String): Resource =
    if(value == null) this
    else {
        val literal = model.createLiteral(value, lang)
        addLiteral(property, literal)
    }

fun Resource.safeAddProperty(property: Property, value: String?): Resource =
    if(value == null) this
    else addProperty(property, value)

fun Resource.safeAddResourceProperty(property: Property, value: Resource?): Resource =
    if(value == null) this
    else addProperty(property, value)

fun Resource.safeAddDateLiteral(property: Property, value: LocalDate?): Resource =
    if(value == null) this
    else {
        val calendar: Calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(value.year, value.monthValue - 1, value.dayOfMonth)
        safeAddLiteral(property, model.createTypedLiteral(calendar))
    }

fun Resource.safeAddLinkedProperty(property: Property, value: String?): Resource =
    if(value == null) this
    else addProperty(property, model.createResource(value))

fun String.addContactStringPrefix(prefix: String): String =
    if (this.startsWith(prefix)) {
        this.replace("\\s", "")
    } else {
        prefix + this.replace("\\s", "")
    }

// -------- Blank Nodes --------

fun Resource.addContactPoints(contactPoints: Collection<Contact>?): Resource {
    contactPoints?.forEach {
        addProperty(DCAT.contactPoint,
            model.createResource(it.uri)
                .addProperty(RDF.type, VCARD4.Organization)
                .safeAddStringLiteral(VCARD4.fn, it.fullname)
                .safeAddProperty(VCARD4.hasURL, it.hasURL)
                .safeAddStringLiteral(VCARD4.organization_name, it.organizationName)
                .safeAddStringLiteral(VCARD4.organization_unit, it.organizationUnit)
                .safeAddLinkedProperty(VCARD4.hasEmail, it.email?.addContactStringPrefix("mailto:"))
                .safeAddLinkedProperty(VCARD4.hasTelephone, it.hasTelephone?.addContactStringPrefix("tel"))
        )
    }
    return this
}

fun Resource.addSkosConcepts(property: Property, skosConcept: Collection<SkosConcept>?, resource: Resource): Resource {
    skosConcept?.forEach {
        addProperty(property,
            model.createResource(it.uri)
                .addProperty(RDF.type, resource)
                .addProperty(RDF.type, SKOS.Concept)
                .safeAddProperty(RDF.type, it.extraType)
                .safeAddProperty(DCTerms.source, it.uri)
                .safeAddLiteralByLang(SKOS.prefLabel, it.prefLabel)
        )
    }
    return this
}

fun Resource.addDistribution(property: Property,distributions: Collection<Distribution>?): Resource {
    distributions?.forEach {
        if (it.hasNonNullProperty()) {
            addProperty(property,
                model.createResource(it.uri)
                    .addProperty(RDF.type, DCAT.distribution)
                    .safeAddStringLiteral(DCTerms.identifier, it.id)
                    .safeAddLiteralByLang(DCTerms.title, it.title)
                    .safeAddLiteralByLang(DCTerms.description, it.description)
                    .safeAddStringListProperty(DCAT.accessURL, it.accessURL)
                    .safeAddStringListProperty(DCAT.downloadURL, it.downloadURL)
                    .addSkosConcepts(DCTerms.license, listOfNotNull(it.license), DCTerms.LicenseDocument)
                    .addSkosConcepts(DCTerms.conformsTo, it.conformsTo, DCTerms.Standard)
                    .addSkosConcepts(FOAF.page, it.page, FOAF.Document)
                    .safeAddStringListLiteral(DCTerms.format, it.format)
                    .addDataDistributionServices(it.accessService)
            )
        }
    }
    return this
}

private fun Distribution.hasNonNullProperty(): Boolean =
    title != null ||
    description != null ||
    accessURL != null ||
    license != null ||
    conformsTo != null ||
    page != null ||
    format != null ||
    accessService != null

// TODO: add dcat:endpointURLs and make sure front-end sends necessary data (https://doc.difi.no/review/dcat-ap-no/#_obligatoriske_egenskaper_for_datatjeneste)
// TODO: add a list of dct:MediaTypes (https://doc.difi.no/review/dcat-ap-no/#distribusjon-medietype)

fun Resource.addDataDistributionServices(dataDistributionServices: Collection<DataDistributionService>?): Resource {
    dataDistributionServices?.forEach {
        addProperty(DCAT.accessService,
            model.createResource(it.uri)
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
                model.createResource()
                    .addProperty(RDF.type, DCTerms.PeriodOfTime)
                    .safeAddDateLiteral(Schema.startDate, it.startDate)
                    .safeAddDateLiteral(Schema.endDate, it.endDate))
        }
    }
    return this
}


fun Resource.addQualityAnnotation(qualityAnnotation: QualityAnnotation?): Resource {
    qualityAnnotation?.let {
        addProperty(DQV.hasQualityAnnotation,
            model.createResource()
                .addProperty(RDF.type, DQV.QualityAnnotation)
                .addQualityAnnotationDimension(DQV.inDimension, it.inDimension)
                .addQualityAnnotationBody(it.hasBody))
    }
    return this
}

fun Resource.addQualityAnnotationDimension(property: Property, dimension: String?): Resource {
    DQV.resolveDimensionResource(dimension)
        ?.let {
            safeAddProperty(property, dimension)
        }
    return this
}

fun Resource.addQualityAnnotationBody(body: Map<String, String>?): Resource {
    body?.let {
        addProperty(OA.hasBody,
            model.createResource()
                .safeAddLiteralByLang(RDF.value, body))
    }
    return this
}

fun String?.notNullOrEmpty() = this != null && this != ""


private fun getReferencePropertyURI(code: String?, uri: String?): String {
    return if (code != null && code != "") {
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
                    model.createResource(it.source?.uri))
            } else {
                addProperty(referenceProperty,
                    model.createResource()
                        .addProperty(RDF.type, DCAT.dataset)
                        .safeAddLiteralByLang(SKOS.prefLabel, it.source?.prefLabel)
                        .safeAddResourceProperty(DCTerms.source, model.createResource(it.source?.uri)))
            }
        }
    }
    return this
}

private fun Reference.isValidReference(): Boolean =
    referenceType != null &&
    source != null &&
    (referenceType.uri.notNullOrEmpty() || referenceType.code.notNullOrEmpty()) &&
    source.uri.notNullOrEmpty()

fun Resource.addRelations(relations: Collection<SkosConcept>?): Resource {
    relations?.forEach {
        addProperty(DCTerms.relation,
        model.createResource(it.uri)
            .addProperty(RDF.type, RDFS.Resource)
            .safeAddLiteralByLang(RDFS.label, it.prefLabel)
        )
    }
    return this
}

fun Resource.addQualifiedAttributions(qualifiedAttributions: Collection<String>?): Resource {
    qualifiedAttributions?.forEach {
        addProperty(PROV.qualifiedAttribution,
            model.createResource()
                .addProperty(RDF.type, PROV.Attribution)
                .safeAddResourceProperty(PROV.agent, ResourceFactory.createResource(URIref.encode("https://data.brreg.no/enhetsregisteret/api/enheter/$it"))))
                .safeAddResourceProperty(DCAT.hadRole, ResourceFactory.createResource(URIref.encode("http://registry.it.csiro.au/def/isotc211/CI_RoleCode/contributor")))
    }
    return this
}

private fun String.getIfNotNullOrEmpty(): String? =
    if (this.isNullOrEmpty())
        null
    else
        this

fun Resource.addPublisher(publisher: Publisher?): Resource {
    publisher?.let {
        addProperty(DCTerms.publisher,
            model.createResource(it.uri?.getIfNotNullOrEmpty() ?: it.id?.getIfNotNullOrEmpty())
                .addProperty(RDF.type, FOAF.Agent)
                .addPublisherName(it)
                .safeAddLiteralByLang(SKOS.prefLabel, it.prefLabel)
                .safeAddStringLiteral(DCTerms.identifier, it.id))
    }
    return this
}

fun Resource.addSubjects(subjects: Collection<Subject>?): Resource {
    subjects?.forEach {
        addProperty(DCTerms.subject,
            model.createResource(it.uri)
                .addProperty(RDF.type, SKOS.Concept)
                .safeAddStringLiteral(DCTerms.identifier, it.identifier)
                .safeAddLangListLiteral(SKOS.altLabel, it.altLabel)
                .safeAddPropertyByLang(SKOS.prefLabel, it.prefLabel)
                .safeAddLiteralByLang(SKOS.definition, it.definition)
                .safeAddPropertyByLang(SKOS.note, it.note)
                .safeAddStringLiteral(DCTerms.source, it.source)
                .safeAddStringListLiteral(SKOS.inScheme, it.inScheme)
                .addPublisher(it.creator))
    }
    return this
}

fun Resource.addLanguages(language:  Collection<SkosCode>?): Resource {
    language?.forEach {
        addProperty(DCTerms.language,
            model.createResource(it.uri)
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