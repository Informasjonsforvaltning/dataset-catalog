package no.fdk.dataset_catalog.rdf

import no.fdk.dataset_catalog.model.Dataset
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF

fun Model.createDatasetResource(ds: Dataset, baseURI: String): Model {
    val model = ModelFactory.createDefaultModel()

    createResource(ds.originalUri ?: ds.uri)
        .addProperty(RDF.type, DCAT.Dataset)
        .safeAddProperty(DCTerms.source, ds.source)
        .safeAddLiteralByLang(DCTerms.title, ds.title)
        .safeAddLiteralByLang(DCTerms.description, ds.description)
        .safeAddLiteralByLang(DCTerms.description, ds.descriptionFormatted)
        .safeAddLiteralByLang(DCATNO.objective, ds.objective)
        .addContactPoints(ds.contactPoint)
        .safeAddLangListProperty(DCAT.keyword, ds.keyword)
        .safeAddDateTypeLiteral(DCTerms.issued, ds.issued)
        .safeAddDateTypeLiteral(DCTerms.modified, ds.modified)
        .safeAddURLs(DCAT.landingPage, ds.landingPage)
        .safeAddLinkListProperty(DCAT.theme, ds.theme?.mapNotNull { it.uri })
        .addDistribution(DCAT.distribution, ds.distribution, baseURI)
        .addDistribution(ADMS.sample, ds.sample, baseURI)
        .addTemporal(ds.temporal)
        .safeAddLinkListProperty(DCTerms.spatial, ds.spatial?.mapNotNull { it.uri })
        .safeAddLinkedProperty(DCTerms.accessRights, ds.accessRights?.uri)
        .safeAddStringListProperty(DCATNO.accessRightsComment, ds.accessRightsComment)
        .addSkosConcepts(DCATNO.legalBasisForRestriction, ds.legalBasisForRestriction, DCTerms.RightsStatement)
        .addSkosConcepts(DCATNO.legalBasisForProcessing, ds.legalBasisForProcessing, DCTerms.RightsStatement)
        .addSkosConcepts(DCATNO.legalBasisForAccess, ds.legalBasisForAccess, DCTerms.RightsStatement)
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
        .addSkosConcepts(DCATNO.informationModel, ds.informationModel, DCTerms.Standard)
        .addQualifiedAttributions(ds.qualifiedAttributions)
        .safeAddProperty(DCTerms.type, ds.type)
        .addPublisher(ds.publisher)
        .addSubjects(ds.subject)
        .addLanguages(ds.language)

    return this
}
