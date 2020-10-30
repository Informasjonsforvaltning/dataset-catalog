package no.fdk.dataset_catalog.rdf

import no.fdk.dataset_catalog.model.Dataset
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms

fun Model.createDatasetResource(ds: Dataset): Model {
    val model = ModelFactory.createDefaultModel()
    model.setNsPrefix("dct", DCTerms.getURI())
    model.setNsPrefix("dcat", DCAT.getURI())
    model.setNsPrefix("dcatno", DCATNO.uri)
    model.setNsPrefix("dcatapi", DCATapi.uri)
    model.setNsPrefix("adms", ADMS.uri)
    model.setNsPrefix("at", AT.uri)
    model.setNsPrefix("prov", PROV.uri)


    createResource(ds.originalUri ?: ds.uri)
        .safeAddProperty(DCTerms.source, ds.source)
        .safeAddLiteralByLang(DCTerms.title, ds.title)
        .safeAddLiteralByLang(DCTerms.description, ds.descriptionFormatted)
        .safeAddLiteralByLang(DCATNO.objective, ds.objective)
        .addContactPoints(ds.contactPoint)
        .safeAddLangListProperty(DCAT.keyword, ds.keyword)
        .safeAddDateLiteral(DCTerms.issued, ds.issued)
        .safeAddDateLiteral(DCTerms.modified, ds.modified)
        .safeAddStringListProperty(DCAT.landingPage, ds.landingPage)
        .safeAddLinkListProperty(DCAT.theme, ds.theme?.mapNotNull { it.uri })
        .addDistribution(DCAT.distribution, ds.distribution)
        .addDistribution(ADMS.sample, ds.sample)
        .addTemporal(ds.temporal)
        .safeAddLinkListProperty(DCTerms.spatial, ds.spatial?.mapNotNull { it.uri })
        .safeAddProperty(DCTerms.accessRights, ds.accessRights?.uri)
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
        .safeAddProperty(DCTerms.provenance, ds.provenance?.uri)
        .safeAddStringListLiteral(DCTerms.identifier, ds.identifier)
        .safeAddStringListProperty(FOAF.page, ds.page)
        .safeAddProperty(DCTerms.accrualPeriodicity, ds.accrualPeriodicity?.uri)
        .safeAddStringListProperty(ADMS.identifier, ds.admsIdentifier)
        .addSkosConcepts(DCTerms.conformsTo, ds.conformsTo, DCTerms.Standard)
        .addSkosConcepts(DCATNO.informationModel, ds.informationModel, DCTerms.Standard)
        .addQualifiedAttributions(ds.qualifiedAttributions)
        .safeAddProperty(DCTerms.type, ds.type)
        .addPublisher(ds.publisher)
        .addSubjects(ds.subjects)
        .addLanguages(ds.language)

    return this
}
