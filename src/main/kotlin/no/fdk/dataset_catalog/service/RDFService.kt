package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.configuration.ApplicationProperties
import no.fdk.dataset_catalog.model.Catalog
import no.fdk.dataset_catalog.rdf.*
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.*
import org.springframework.stereotype.Service


@Service
class RDFService(private val catalogService: CatalogService,
                 private val datasetService: DatasetService,
                 private val applicationProperties: ApplicationProperties) {

    fun getAll(): Model? = catalogService.getAll().createCatalogModel()

    fun getById(catalogId: String): Model? =
        catalogService.getByID(catalogId)
            ?.let { listOf(it).createCatalogModel() }

    private fun List<Catalog>.createCatalogModel(): Model {
        val model = ModelFactory.createDefaultModel()
        model.setNsPrefix("dct", DCTerms.NS)
        model.setNsPrefix("dcat", DCAT.NS)
        model.setNsPrefix("dcatno", DCATNO.uri)
        model.setNsPrefix("adms", ADMS.uri)
        model.setNsPrefix("prov", PROV.uri)
        model.setNsPrefix("rdfs", RDFS.uri)
        model.setNsPrefix("foaf", FOAF.NS)
        model.setNsPrefix("vcard", VCARD4.NS)
        model.setNsPrefix("xsd", XSD.NS)
        model.setNsPrefix("iso", DQV.ISO)
        model.setNsPrefix("dqv", DQV.uri)
        model.setNsPrefix("rdf", RDF.uri)
        model.setNsPrefix("skos", SKOS.uri)
        model.setNsPrefix("oa", OA.NS)
        model.setNsPrefix("dc", DC.NS)
        model.setNsPrefix("schema", "http://schema.org/")

        forEach {
            val baseUri = "${applicationProperties.conceptCatalogueHost}/${it.id}/datasets/"

            model.createResource(it.uri)
                .addProperty(RDF.type, DCAT.Catalog)
                .safeAddPropertyByLang(DCTerms.title, it.title)
                .safeAddPropertyByLang(DCTerms.description, it.description)
                .safeAddDateTimeLiteral(DCTerms.issued, it.issued)
                .safeAddDateTimeLiteral(DCTerms.modified, it.modified)
                .safeAddProperty(DCTerms.language, it.language)
                .addPublisher(it.publisher)
                .addDatasets(
                    it.id?.let { catId -> datasetService.getAll(catId) },
                    baseUri)
        }

        return model
    }
}

