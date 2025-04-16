package no.fdk.dataset_catalog.rdf

import no.fdk.dataset_catalog.model.DatasetDBO
import no.fdk.dataset_catalog.model.SpecializedType
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF

fun Model.addDatasetResource(dataset: DatasetDBO, seriesData: SeriesData, baseCatalogURI: String): Resource {
    val datasetURI = when {
        dataset.originalUri.isValidURL() -> dataset.originalUri
        dataset.uri.isValidURL() -> dataset.uri
        else -> "${baseCatalogURI}/${dataset.catalogId}/datasets/${dataset.id}"
    }
    val datasetResource = safeCreateResource(datasetURI)
        .safeAddLocalizedString(DCTerms.title, dataset.title)
        .safeAddLocalizedString(DCTerms.description, dataset.description)

    if (dataset.specializedType == SpecializedType.SERIES) {
        datasetResource
            .addProperty(RDF.type, ResourceFactory.createProperty("${DCAT.getURI()}DatasetSeries"))
            .safeAddLinkedProperty(ResourceFactory.createProperty("${DCAT.getURI()}first"), seriesData.first)
            .safeAddLinkedProperty(ResourceFactory.createProperty("${DCAT.getURI()}last"), seriesData.last)
    } else {
        datasetResource
            .addProperty(RDF.type, DCAT.Dataset)
            .addContactPoints(dataset.contactPoints)
            .safeAddLocalizedStringList(DCAT.keyword, dataset.keywords)
            .safeAddDateTimeLiteral(DCTerms.issued, dataset.issued)
            .safeAddDateTimeLiteral(DCTerms.modified, dataset.modified)
            .safeAddURLs(DCAT.landingPage, dataset.landingPage)
            .addDatasetThemes(dataset)
            .addDatasetDistribution(DCAT.distribution, dataset.distribution)
            .addDatasetDistribution(ADMS.sample, dataset.sample)
            .addTemporal(dataset.temporal)
            .safeAddLinkListProperty(DCTerms.spatial, dataset.spatial?.mapNotNull { it })
            .safeAddLinkedProperty(DCTerms.accessRights, dataset.accessRight)
            .addLegalBasis(dataset)
            .addQualityAnnotation(dataset.accuracy)
            .addQualityAnnotation(dataset.completeness)
            .addQualityAnnotation(dataset.currentness)
            .addQualityAnnotation(dataset.availability)
            .addQualityAnnotation(dataset.relevance)
            .addReferences(dataset.references)
            .addRelatedResources(dataset.relatedResources)
            .safeAddLinkedProperty(DCTerms.provenance, dataset.provenance)
            .safeAddLinkedProperty(DCTerms.accrualPeriodicity, dataset.frequency)
            .addDistributionConformsTo(dataset.conformsTo)
            .addDistributionConformsTo(dataset.informationModelsFromOtherSources)
            .addConformsToFromListOfUris(dataset.informationModelsFromFDK)
            .addQualifiedAttributions(dataset.qualifiedAttributions)
            .addDatasetType(dataset.type)
            .addConcepts(dataset.concepts)
            .addLanguages(dataset.language)
            .safeAddLinkedProperty(ResourceFactory.createProperty("${DCAT.getURI()}inSeries"), seriesData.inSeries)
            .safeAddLinkedProperty(ResourceFactory.createProperty("${DCAT.getURI()}next"), seriesData.next)
            .safeAddLinkedProperty(ResourceFactory.createProperty("${DCAT.getURI()}prev"), seriesData.prev)
    }
    return datasetResource
}
