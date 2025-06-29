package no.fdk.dataset_catalog.rdf

import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.model.REGISTRATION_STATUS
import no.fdk.dataset_catalog.model.SpecializedType
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF

fun Model.addDatasetResource(dataset: Dataset, seriesData: SeriesData, baseCatalogURI: String): Resource {
    val datasetURI = when {
        dataset.originalUri.isValidURL() -> dataset.originalUri
        dataset.uri.isValidURL() -> dataset.uri
        else -> "${baseCatalogURI}/${dataset.catalogId}/datasets/${dataset.id}"
    }
    val datasetResource = safeCreateResource(datasetURI)
        .safeAddLiteralByLang(DCTerms.title, dataset.title)
        .safeAddLiteralByLang(DCTerms.description, dataset.description)
        .safeAddStringListLiteral(DCTerms.identifier, dataset.dctIdentifier())

    if (dataset.specializedType == SpecializedType.SERIES) {
        datasetResource
            .addProperty(RDF.type, ResourceFactory.createProperty("${DCAT.getURI()}DatasetSeries"))
            .safeAddLinkedProperty(ResourceFactory.createProperty("${DCAT.getURI()}first"), seriesData.first)
            .safeAddLinkedProperty(ResourceFactory.createProperty("${DCAT.getURI()}last"), seriesData.last)
    } else {
        datasetResource
            .addProperty(RDF.type, DCAT.Dataset)
            .safeAddProperty(DCTerms.source, dataset.source)
            .addContactPoints(dataset.contactPoint)
            .safeAddLangListProperty(DCAT.keyword, dataset.keyword)
            .safeAddDateTimeLiteral(DCTerms.issued, dataset.issued)
            .safeAddDateTimeLiteral(DCTerms.modified, dataset.modified)
            .safeAddURLs(DCAT.landingPage, dataset.landingPage)
            .addThemes(dataset)
            .addDistribution(DCAT.distribution, dataset.distribution)
            .addDistribution(ADMS.sample, dataset.sample)
            .addTemporal(dataset.temporal)
            .safeAddLinkListProperty(DCTerms.spatial, dataset.spatial?.mapNotNull { it.uri })
            .safeAddLinkedProperty(DCTerms.accessRights, dataset.accessRights?.uri)
            .addCPSVNORules(dataset)
            .addQualityAnnotation(dataset.hasAccuracyAnnotation?.hasBody, DQV.Accuracy)
            .addQualityAnnotation(dataset.hasCompletenessAnnotation?.hasBody, DQV.Completeness)
            .addQualityAnnotation(dataset.hasCurrentnessAnnotation?.hasBody, DQV.Currentness)
            .addQualityAnnotation(dataset.hasAvailabilityAnnotation?.hasBody, DQV.Availability)
            .addQualityAnnotation(dataset.hasRelevanceAnnotation?.hasBody, DQV.Relevance)
            .addReferences(dataset.references)
            .addRelations(dataset.relations)
            .safeAddLinkedProperty(DCTerms.provenance, dataset.provenance?.uri)
            .safeAddLinkListProperty(FOAF.page, dataset.page)
            .safeAddLinkedProperty(DCTerms.accrualPeriodicity, dataset.accrualPeriodicity?.uri)
            .safeAddLinkListProperty(ADMS.identifier, dataset.admsIdentifier)
            .addConformsTo(dataset.conformsTo)
            .addConformsTo(dataset.informationModel)
            .addConformsToFromListOfUris(dataset.informationModelsFromFDK)
            .addQualifiedAttributions(dataset.qualifiedAttributions)
            .addDatasetType(dataset.type)
            .addPublisher(dataset.publisher)
            .addSubjects(dataset.concepts)
            .addLanguages(dataset.language)
            .safeAddLinkedProperty(ResourceFactory.createProperty("${DCAT.getURI()}inSeries"), seriesData.inSeries)
            .safeAddLinkedProperty(ResourceFactory.createProperty("${DCAT.getURI()}next"), seriesData.next)
            .safeAddLinkedProperty(ResourceFactory.createProperty("${DCAT.getURI()}prev"), seriesData.prev)
    }
    return datasetResource
}
