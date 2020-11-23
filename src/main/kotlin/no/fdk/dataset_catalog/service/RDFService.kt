package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.model.Catalog
import no.fdk.dataset_catalog.rdf.*
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Service


@Service
class RDFService(private val catalogService: CatalogService,
                 private val datasetService: DatasetService) {

    fun getAll(): Model? = catalogService.getAll().createCatalogModel()

    fun getById(catalogId: String): Model? =
        catalogService.getByID(catalogId)
            ?.let { listOf(it).createCatalogModel() }

    private fun List<Catalog>.createCatalogModel(): Model {
        val model = ModelFactory.createDefaultModel()
        model.setNsPrefix("dct", DCTerms.getURI())
        model.setNsPrefix("dcat", DCAT.getURI())

        forEach{
            model.createResource(it.uri)
                .addProperty(RDF.type, DCAT.catalog)
                .safeAddPropertyByLang(DCTerms.title, it.title)
                .safeAddPropertyByLang(DCTerms.description, it.description)
                .safeAddLinkedProperty(DCTerms.publisher, it.publisher?.uri)
                .safeAddDateLiteral(DCTerms.issued, it.issued)
                .safeAddDateLiteral(DCTerms.modified, it.modified)
                .safeAddProperty(DCTerms.language, it.language)
                .safeAddDatasetListLinkedProperty(DCAT.dataset, it.dataset)
                .addPublisher(it.publisher)

            it.id?.let {catId -> datasetService.getAll(catId).forEach { dataset ->  model.createDatasetResource(dataset) } }
        }

        return model
    }
}

