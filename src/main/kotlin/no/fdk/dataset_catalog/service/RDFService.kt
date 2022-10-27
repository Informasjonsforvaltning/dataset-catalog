package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.model.Catalog
import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.model.REGISTRATION_STATUS
import no.fdk.dataset_catalog.rdf.*
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.*
import org.springframework.stereotype.Service


@Service
class RDFService(private val catalogService: CatalogService,
                 private val datasetService: DatasetService) {

    fun getAll(): Model = catalogService.getAll().createCatalogModel()

    fun getCatalogById(catalogId: String): Model? =
        catalogService.getByID(catalogId)
            ?.let { listOf(it).createCatalogModel() }

    fun getDatasetById(catalogId: String, id: String): Model? =
        datasetService.getByID(catalogId, id)
            ?.takeIf { it.registrationStatus == REGISTRATION_STATUS.PUBLISH }
            ?.let { it.copy(references = datasetService.resolveReferences(it)) }
            ?.createModel()

    private fun List<Catalog>.createCatalogModel(): Model {
        val model = ModelFactory.createDefaultModel()
        model.setDefaultPrefixes()

        forEach { catalog ->
            model.createResource(catalog.uri)
                .addProperty(RDF.type, DCAT.Catalog)
                .safeAddPropertyByLang(DCTerms.title, catalog.title)
                .safeAddPropertyByLang(DCTerms.description, catalog.description)
                .safeAddDateTimeLiteral(DCTerms.issued, catalog.issued)
                .safeAddDateTimeLiteral(DCTerms.modified, catalog.modified)
                .safeAddProperty(DCTerms.language, catalog.language)
                .addPublisher(catalog.publisher)
                .addDatasets(
                    catalog.id?.let { catId -> datasetService.getAll(catId).map {
                        it.copy(references = datasetService.resolveReferences(it))
                    } })
        }

        return model
    }

    private fun Dataset.createModel(): Model {
        val model = ModelFactory.createDefaultModel()
        model.setDefaultPrefixes()
        model.addDatasetResource(this)
        return model
    }

    private fun Model.setDefaultPrefixes() {
        setNsPrefix("dct", DCTerms.NS)
        setNsPrefix("dcat", DCAT.NS)
        setNsPrefix("adms", ADMS.uri)
        setNsPrefix("prov", PROV.uri)
        setNsPrefix("rdfs", RDFS.uri)
        setNsPrefix("foaf", FOAF.NS)
        setNsPrefix("vcard", VCARD4.NS)
        setNsPrefix("xsd", XSD.NS)
        setNsPrefix("iso", DQV.ISO)
        setNsPrefix("dqv", DQV.uri)
        setNsPrefix("rdf", RDF.uri)
        setNsPrefix("skos", SKOS.uri)
        setNsPrefix("oa", OA.NS)
        setNsPrefix("schema", "http://schema.org/")
        setNsPrefix("cpsv", CPSV.uri)
        setNsPrefix("cpsvno", CPSVNO.uri)
        setNsPrefix("eli", ELI.uri)
    }
}
