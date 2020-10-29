package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.model.Catalog
import no.fdk.dataset_catalog.rdf.*
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Service


@Service
class RDFService(val catalogService: CatalogService) {

    fun getAll(): Model? = catalogService.getAll().createCatalogModel()

    fun getById(catalogId: String): Model? =
        catalogService.getByID(catalogId)
            ?.let { listOf(it).createCatalogModel() }
}

private fun List<Catalog>.createCatalogModel(): Model {
    val model = ModelFactory.createDefaultModel()
    model.setNsPrefix("dct", DCTerms.getURI())
    model.setNsPrefix("dcat", DCAT.getURI())

    forEach{
        model.createResource(it.uri)
            .addProperty(RDF.type, DCAT.catalog)
            .addPropertyByLang(DCTerms.title, it.title)
            .addPropertyByLang(DCTerms.description, it.description)
            .safeAddLinkedProperty(DCTerms.publisher, it.publisher?.uri)
            .safeAddDateProperty(DCTerms.issued, it.issued)
            .safeAddDateProperty(DCTerms.modified, it.modified)
            .safeAddProperty(DCTerms.language, it.language)
            .safeAddDatasetListLinkedProperty(DCAT.dataset, it.dataset)

        if (it.publisher != null) {
            model.createResource(it.publisher.uri)
                .addProperty(RDF.type, FOAF.Agent)
                .safeAddProperty(DCTerms.identifier, it.publisher.id)
                .addPublisherName(it.publisher)
        }

        it.dataset?.forEach { model.createDatasetResource(it) }
    }

    return model
}