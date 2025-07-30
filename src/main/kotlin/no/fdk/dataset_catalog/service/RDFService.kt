package no.fdk.dataset_catalog.service

import no.fdk.dataset_catalog.configuration.ApplicationProperties
import no.fdk.dataset_catalog.model.CatalogCount
import no.fdk.dataset_catalog.model.DatasetDBO
import no.fdk.dataset_catalog.model.SpecializedType
import no.fdk.dataset_catalog.rdf.*
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.*
import org.springframework.stereotype.Service


@Service
class RDFService(
    private val catalogService: CatalogService,
    private val datasetService: DatasetService,
    private val applicationProperties: ApplicationProperties
) {

    fun getAll(): Model = catalogService.getAll().createCatalogModel()

    fun getCatalogById(catalogId: String): Model? =
        catalogService.getByID(catalogId)
            ?.let { listOf(it).createCatalogModel() }

    fun getDatasetById(catalogId: String, id: String): Model? =
        datasetService.getDatasetByID(catalogId, id)
            ?.takeIf { it.published == true }
            ?.let { it.copy(references = datasetService.resolveDatasetReferences(it)) }
            ?.createModel()

    private fun List<CatalogCount>.createCatalogModel(): Model {
        val model = ModelFactory.createDefaultModel()
        model.setDefaultPrefixes()

        forEach { catalog ->
            model.createResource("${applicationProperties.catalogUriHost}/${catalog.id}")
                .addProperty(RDF.type, DCAT.Catalog)
                .addProperty(DCTerms.title, "Dataset catalog belonging to ${catalog.id}", "en")
                .addProperty(
                    DCTerms.publisher,
                    model.safeCreateResource(organizationCatalogURI(catalog.id))
                )
                .addDatasets(
                    datasetService.getAllDatasets(catalog.id).map {
                        it.copy(references = datasetService.resolveDatasetReferences(it))
                    }
                )
        }

        return model
    }

    private fun Resource.addDatasets(datasets: List<DatasetDBO>?): Resource {
        datasets?.forEach { dataset ->
            val seriesData = dataset.seriesData()
            val isPublished = dataset.published == true
            when {
                seriesData.inSeries != null && isPublished -> model.addDatasetResource(
                    dataset,
                    seriesData,
                    applicationProperties.catalogUriHost,
                    organizationCatalogURI(dataset.catalogId)
                ) // A dataset in a series shouldn't be associated with a catalog through dcat:dataset, only indirectly from a series in the catalog
                isPublished -> addProperty(
                    DCAT.dataset,
                    model.addDatasetResource(
                        dataset,
                        seriesData,
                        applicationProperties.catalogUriHost,
                        organizationCatalogURI(dataset.catalogId)
                    )
                )
            }
        }
        return this
    }

    private fun DatasetDBO.createModel(): Model {
        val model = ModelFactory.createDefaultModel()
        model.setDefaultPrefixes()
        model.addDatasetResource(this, seriesData(), applicationProperties.catalogUriHost, organizationCatalogURI(catalogId))
        return model
    }

    private fun DatasetDBO.seriesData(): SeriesData =
        if (catalogId == null) SeriesData(null, null, null, null, null)
        else {
            val seriesDataset = inSeries?.let { datasetService.getDatasetByID(catalogId, inSeries) }
                ?.takeIf { it.published == true && it.specializedType == SpecializedType.SERIES }

            val orderValue = seriesDataset?.seriesDatasetOrder?.get(id)

            // next and prev is calculated from the published datasets in the order from the series linked to with inSeries
            val nextAndPrev = if (orderValue == null) null
            else seriesDataset.seriesDatasetOrder.keys
                .let { datasetService.getDatasetListByIDs(catalogId, it.toList()) }
                .filter { it.published == true }
                .associate { it.getURI() to seriesDataset.seriesDatasetOrder[it.id] }
                .filterNotNullKeysAndValues()
                .let { publishedOrder ->
                    Pair(
                        publishedOrder.filter { it.value > orderValue }
                            .minByOrNull { it.value }?.key,
                        publishedOrder.filter { it.value < orderValue }
                            .maxByOrNull { it.value }?.key
                    )
                }

            // first and last is calculated from the published datasets in the series order
            val firstAndLast = seriesDatasetOrder?.keys
                ?.takeIf { specializedType == SpecializedType.SERIES }
                ?.let { datasetService.getDatasetListByIDs(catalogId, it.toList()) }
                ?.filter { it.published == true }
                ?.associate { it.getURI() to seriesDatasetOrder[it.id] }
                ?.filterNotNullKeysAndValues()
                ?.let { publishedOrder ->
                    Pair(
                        publishedOrder.minByOrNull { it.value }?.key,
                        publishedOrder.maxByOrNull { it.value }?.key
                    )
                }

            SeriesData(
                inSeries = seriesDataset?.getURI(),
                next = nextAndPrev?.first,
                prev = nextAndPrev?.second,
                first = firstAndLast?.first,
                last = firstAndLast?.second
            )
        }

    private fun <K, V> Map<K?, V?>.filterNotNullKeysAndValues(): Map<K, V> =
        mapNotNull { (key, value) -> value?.let { key to it } }
            .mapNotNull { (key, value) -> key?.let { it to value } }
            .toMap()

    private fun DatasetDBO.getURI(): String? =
        originalUri ?: uri

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

    private fun organizationCatalogURI(organizationNumber: String) =
        "${applicationProperties.organizationCatalogHost}/organizations/$organizationNumber"
}
