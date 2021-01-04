package no.fdk.dataset_catalog.extensions

import no.fdk.dataset_catalog.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun List<Dataset>.toDTO() : DatasetEmbeddedWrapperDTO = DatasetEmbeddedWrapperDTO(mapOf(Pair("datasets", this)))

fun Dataset.updateSubjects(): Dataset =
    copy(subject = concepts?.map {
        Subject(
            id = it.id,
            uri = it.uri,
            definition = it.definition?.text,
            prefLabel = it.prefLabel,
            altLabel = it.altLabel,
            identifier = it.identifier
        )
    })

fun Dataset.update(newValues: DatasetDTO): Dataset =
    copy(
        lastModified = LocalDateTime.now(),
        registrationStatus = newValues.registrationStatus ?: registrationStatus,
        concepts = newValues.concepts ?: concepts,
        subject = newValues.subject ?: subject,
        uri = newValues.uri ?: uri,
        originalUri = newValues.originalUri ?: originalUri,
        source = newValues.source ?: source,
        title = newValues.title ?: title,
        description = newValues.description ?: description,
        descriptionFormatted = newValues.descriptionFormatted,
        objective = newValues.objective ?: objective,
        contactPoint = newValues.contactPoint ?: contactPoint,
        keyword = newValues.keyword ?: keyword,
        publisher = newValues.publisher ?: publisher,
        issued = updateTime(newValues.issued, issued),
        modified = updateTime(newValues.modified, modified),
        language = newValues.language ?: language,
        landingPage = newValues.landingPage ?: landingPage,
        theme = newValues.theme ?: theme,
        distribution = newValues.distribution ?: distribution,
        sample = newValues.sample ?: sample,
        temporal = updatePeriodsOfTime(newValues.temporal, temporal),
        spatial = newValues.spatial ?: spatial,
        accessRights = newValues.accessRights ?: accessRights,
        accessRightsComment = newValues.accessRightsComment ?: accessRightsComment,
        legalBasisForRestriction = newValues.legalBasisForRestriction ?: legalBasisForRestriction,
        legalBasisForProcessing = newValues.legalBasisForProcessing ?: legalBasisForProcessing,
        legalBasisForAccess = newValues.legalBasisForAccess ?: legalBasisForAccess,
        hasAccuracyAnnotation = newValues.hasAccuracyAnnotation ?: hasAccuracyAnnotation,
        hasCompletenessAnnotation = newValues.hasCompletenessAnnotation ?: hasCompletenessAnnotation,
        hasCurrentnessAnnotation = newValues.hasCurrentnessAnnotation ?: hasCurrentnessAnnotation,
        hasAvailabilityAnnotation = newValues.hasAvailabilityAnnotation ?: hasAvailabilityAnnotation,
        hasRelevanceAnnotation = newValues.hasRelevanceAnnotation ?: hasRelevanceAnnotation,
        references = newValues.references ?: references,
        relations = newValues.relations ?: relations,
        provenance = newValues.provenance ?: provenance,
        identifier = newValues.identifier ?: identifier,
        page = newValues.page ?: page,
        accrualPeriodicity = newValues.accrualPeriodicity ?: accrualPeriodicity,
        admsIdentifier = newValues.admsIdentifier ?: admsIdentifier,
        conformsTo = newValues.conformsTo ?: conformsTo,
        informationModel = newValues.informationModel ?: informationModel,
        qualifiedAttributions = newValues.qualifiedAttributions ?: qualifiedAttributions,
        type = newValues.type ?: type,
        catalog = newValues.catalog ?: catalog,
    )

private fun updateTime(dtoTime: String?, dboTime: LocalDate?): LocalDate? {
    return try {
        LocalDate.parse(dtoTime, dateTimeFormatter)
    } catch (e: Exception) {
        if (dtoTime == null) {
            dboTime
        } else {
            null
        }
    }
}

private fun updatePeriodsOfTime(dtoTime: List<PeriodOfTimeDTO>?, dboTime: List<PeriodOfTime>?): List<PeriodOfTime>? =
    dtoTime?.map {
        PeriodOfTime(
            id = it.id,
            name = it.name,
            startDate = updateTime(it.startDate, null),
            endDate = updateTime(it.endDate, null)
        )
    } ?: dboTime