package no.fdk.dataset_catalog.rdf

import no.fdk.dataset_catalog.model.Dataset
import no.fdk.dataset_catalog.model.REGISTRATION_STATUS
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF

fun Resource.addDatasets(datasets: List<Dataset>?): Resource {
    datasets?.forEach {
        createDatasetResource(it)
    }
    return this
}


fun Resource.createDatasetResource(ds: Dataset): Resource {
    if (ds.registrationStatus == REGISTRATION_STATUS.PUBLISH) {
        addProperty(DCAT.dataset, model.addDatasetResource(ds))
    }

    return this
}

fun Model.addDatasetResource(ds: Dataset): Resource =
    safeCreateResource(ds.originalUri ?: ds.uri)
        .addProperty(RDF.type, DCAT.Dataset)
        .safeAddProperty(DCTerms.source, ds.source)
        .safeAddLiteralByLang(DCTerms.title, ds.title)
        .safeAddLiteralByLang(DCTerms.description, ds.description)
        .addContactPoints(ds.contactPoint)
        .safeAddLangListProperty(DCAT.keyword, ds.keyword)
        .safeAddDateTimeLiteral(DCTerms.issued, ds.issued)
        .safeAddDateTimeLiteral(DCTerms.modified, ds.modified)
        .safeAddURLs(DCAT.landingPage, ds.landingPage)
        .safeAddLinkListProperty(DCAT.theme, ds.theme?.mapNotNull { it.uri })
        .addDistribution(DCAT.distribution, ds.distribution, ds.uri)
        .addDistribution(ADMS.sample, ds.sample, ds.uri)
        .addTemporal(ds.temporal)
        .safeAddLinkListProperty(DCTerms.spatial, ds.spatial?.mapNotNull { it.uri })
        .safeAddLinkedProperty(DCTerms.accessRights, ds.accessRights?.uri)
        .addCPSVNORules(ds)
        .addQualityAnnotation(ds.hasAccuracyAnnotation)
        .addQualityAnnotation(ds.hasCompletenessAnnotation)
        .addQualityAnnotation(ds.hasCurrentnessAnnotation)
        .addQualityAnnotation(ds.hasAvailabilityAnnotation)
        .addQualityAnnotation(ds.hasRelevanceAnnotation)
        .addReferences(ds.references)
        .addRelations(ds.relations)
        .safeAddLinkedProperty(DCTerms.provenance, ds.provenance?.uri)
        .safeAddStringListLiteral(DCTerms.identifier, ds.identifier)
        .safeAddLinkListProperty(FOAF.page, ds.page)
        .safeAddLinkedProperty(DCTerms.accrualPeriodicity, ds.accrualPeriodicity?.uri)
        .safeAddLinkListProperty(ADMS.identifier, ds.admsIdentifier)
        .addSkosConcepts(DCTerms.conformsTo, ds.conformsTo, DCTerms.Standard)
        .addSkosConcepts(DCTerms.conformsTo, ds.informationModel, DCTerms.Standard)
        .addQualifiedAttributions(ds.qualifiedAttributions)
        .safeAddProperty(DCTerms.type, ds.type)
        .addPublisher(ds.publisher)
        .addSubjects(ds.subject)
        .addLanguages(ds.language)
