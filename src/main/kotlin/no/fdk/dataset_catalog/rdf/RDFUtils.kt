package no.fdk.dataset_catalog.rdf

import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.model.Publisher
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.*

fun Resource.addPublisherName(publisher: Publisher): Resource {
    if (publisher.name != null && publisher.prefLabel == null){
        addProperty(FOAF.name, publisher.name, "nb")
    } else {
        if (publisher.prefLabel?.get("nb") != null) addProperty(FOAF.name, publisher.prefLabel["nb"], "nb")
        if (publisher.prefLabel?.get("nn") != null) addProperty(FOAF.name, publisher.prefLabel["nn"], "nn")
        if (publisher.prefLabel?.get("en") != null) addProperty(FOAF.name, publisher.prefLabel["en"], "en")
    }
    return this
}

fun Resource.addPropertyByLang(property: Property, langMap: Map<String, String>?): Resource {
    if (langMap?.get("nb") != null) addProperty(property, langMap["nb"], "nb")
    if (langMap?.get("nn") != null) addProperty(property, langMap["nn"], "nn")
    if (langMap?.get("en") != null) addProperty(property, langMap["en"], "en")
    return this
}

fun Resource.safeAddDatasetListLinkedProperty(property: Property, value: List<Dataset>?): Resource =
    if(value == null || value.isEmpty()) this
    else {
        value.map { addProperty(property, model.createResource(it.originalUri ?: it.uri)) }
        this
    }

fun Resource.safeAddProperty(property: Property, value: String?): Resource =
    if(value == null) this
    else addProperty(property, value)

fun Resource.safeAddDateProperty(property: Property, value: LocalDate?): Resource =
    if(value == null) this
    else {
        val calendar: Calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(value.year, value.monthValue-1, value.dayOfMonth)
        addProperty(property, model.createTypedLiteral(calendar))
    }

fun Resource.safeAddLinkedProperty(property: Property, value: String?): Resource =
    if(value == null) this
    else addProperty(property, model.createResource(value))

fun acceptHeaderToJenaType(accept: String?): JenaType =
    when (accept) {
        "text/turtle" -> JenaType.TURTLE
        else -> JenaType.NOT_ACCEPTABLE
    }

enum class JenaType(val value: String){
    TURTLE("TURTLE"),
    NOT_ACCEPTABLE("")
}

fun Model.createRDFResponse(): String =
    ByteArrayOutputStream().use{ out ->
        write(out, "TURTLE")
        out.flush()
        out.toString("UTF-8")
    }