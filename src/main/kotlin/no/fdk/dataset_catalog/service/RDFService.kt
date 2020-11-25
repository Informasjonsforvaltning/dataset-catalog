package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.configuration.ApplicationProperties
import no.fdk.dataset_catalog.model.Catalog
import no.fdk.dataset_catalog.rdf.*
import org.apache.jena.datatypes.xsd.XSDDatatype
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
        model.setNsPrefix("dcatapi", DCATapi.uri)
        model.setNsPrefix("adms", ADMS.uri)
        model.setNsPrefix("prov", PROV.uri)
        model.setNsPrefix("rdfs", RDFS.uri)
        model.setNsPrefix("foaf", FOAF.NS)
        model.setNsPrefix("vcard", VCARD4.NS)
        model.setNsPrefix("dcatapi", DCATapi.uri)
        model.setNsPrefix("xsd", XSD.NS)
        model.setNsPrefix("iso", DQV.uri)
        model.setNsPrefix("dqv", DQV.uri)
        model.setNsPrefix("rdf", RDF.uri)
        model.setNsPrefix("skos", SKOS.uri)
        model.setNsPrefix("schema", "http://schema.org/")



        forEach{
            model.createResource(it.uri)
                .addProperty(RDF.type, DCAT.Catalog)
                .safeAddPropertyByLang(DCTerms.title, it.title)
                .safeAddPropertyByLang(DCTerms.description, it.description)
                .safeAddLinkedProperty(DCTerms.publisher, it.publisher?.uri)
                .safeAddDateTypeLiteral(DCTerms.issued, it.issued)
                .safeAddDateTypeLiteral(DCTerms.modified, it.modified)
                .safeAddProperty(DCTerms.language, it.language)
                .safeAddDatasetListLinkedProperty(DCAT.dataset, it.dataset)
                .addPublisher(it.publisher)



            val catalogDatasets = it.id?.let { catId -> datasetService.getAll(catId) }
            catalogDatasets?.forEach {
                dataset ->  model.createDatasetResource(
                dataset,
                "${applicationProperties.conceptCatalogueHost}/${it.id}/datasets/${dataset.id}") }
        }

        return model
    }
}

