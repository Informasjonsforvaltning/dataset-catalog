package no.fdk.dataset_catalog.rdf

import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.model.REGISTRATION_STATUS
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF

fun Model.addDatasetResource(dataset: Dataset, seriesData: SeriesData): Resource =
    safeCreateResource(dataset.originalUri ?: dataset.uri)
        .addDatasetRDFType(dataset.specializedType)
        .safeAddProperty(DCTerms.source, dataset.source)
        .safeAddLiteralByLang(DCTerms.title, dataset.title)
        .safeAddLiteralByLang(DCTerms.description, dataset.description)
        .addContactPoints(dataset.contactPoint)
        .safeAddLangListProperty(DCAT.keyword, dataset.keyword)
        .safeAddDateTimeLiteral(DCTerms.issued, dataset.issued)
        .safeAddDateTimeLiteral(DCTerms.modified, dataset.modified)
        .safeAddURLs(DCAT.landingPage, dataset.landingPage)
        .safeAddLinkListProperty(DCAT.theme, dataset.theme?.mapNotNull { it.uri })
        .addDistribution(DCAT.distribution, dataset.distribution)
        .addDistribution(ADMS.sample, dataset.sample)
        .addTemporal(dataset.temporal)
        .safeAddLinkListProperty(DCTerms.spatial, dataset.spatial?.mapNotNull { it.uri })
        .safeAddLinkedProperty(DCTerms.accessRights, dataset.accessRights?.uri)
        .addCPSVNORules(dataset)
        .addQualityAnnotation(dataset.hasAccuracyAnnotation)
        .addQualityAnnotation(dataset.hasCompletenessAnnotation)
        .addQualityAnnotation(dataset.hasCurrentnessAnnotation)
        .addQualityAnnotation(dataset.hasAvailabilityAnnotation)
        .addQualityAnnotation(dataset.hasRelevanceAnnotation)
        .addReferences(dataset.references)
        .addRelations(dataset.relations)
        .safeAddLinkedProperty(DCTerms.provenance, dataset.provenance?.uri)
        .safeAddStringListLiteral(DCTerms.identifier, dataset.dctIdentifier())
        .safeAddLinkListProperty(FOAF.page, dataset.page)
        .safeAddLinkedProperty(DCTerms.accrualPeriodicity, dataset.accrualPeriodicity?.uri)
        .safeAddLinkListProperty(ADMS.identifier, dataset.admsIdentifier)
        .addConformsTo(dataset.conformsTo)
        .addConformsTo(dataset.informationModel)
        .addQualifiedAttributions(dataset.qualifiedAttributions)
        .safeAddProperty(DCTerms.type, dataset.type)
        .addPublisher(dataset.publisher)
        .addSubjects(dataset.concepts)
        .addLanguages(dataset.language)
        .safeAddProperty(ResourceFactory.createProperty("${DCAT.getURI()}inSeries"), seriesData.inSeries)
        .safeAddProperty(ResourceFactory.createProperty("${DCAT.getURI()}next"), seriesData.next)
        .safeAddProperty(ResourceFactory.createProperty("${DCAT.getURI()}prev"), seriesData.prev)
        .safeAddProperty(ResourceFactory.createProperty("${DCAT.getURI()}first"), seriesData.first)
        .safeAddProperty(ResourceFactory.createProperty("${DCAT.getURI()}last"), seriesData.last)
