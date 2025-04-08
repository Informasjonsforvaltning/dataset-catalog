package no.fdk.dataset_catalog.extensions

import no.fdk.dataset_catalog.model.*
import java.time.LocalDateTime
import javax.xml.crypto.Data

fun List<Dataset>.toDTO(): DatasetEmbeddedWrapperDTO = DatasetEmbeddedWrapperDTO(mapOf(Pair("datasets", this)))

fun Dataset.datasetToDBO(): DatasetDBO =
    DatasetDBO(
        id = id!!,
        catalogId = catalogId!!,
        uri = uri,

        lastModified = lastModified,
        specializedType = specializedType,
        originalUri = originalUri,
        published = registrationStatus == REGISTRATION_STATUS.PUBLISH,
        approved = registrationStatus == REGISTRATION_STATUS.APPROVE || registrationStatus == REGISTRATION_STATUS.PUBLISH,

        concepts = concepts?.mapNotNull { it.uri }?.toSet(),
        title = title?.let { LocalizedStrings(it.get("nb"), it.get("nn"), it.get("en")) },
        description = description?.let { LocalizedStrings(it.get("nb"), it.get("nn"), it.get("en")) },
        contactPoints = contactPoint?.map {
            ContactPoint(
                name = it.organizationUnit?.let { name -> LocalizedStrings(nb = name) },
                email = it.email,
                url = it.hasURL,
                phone = it.hasTelephone
            )
        },
        keywords = keyword?.let {
            LocalizedStringLists(
                nb = it.mapNotNull { keyword -> keyword.get("nb") },
                nn = it.mapNotNull { keyword -> keyword.get("nn") },
                en = it.mapNotNull { keyword -> keyword.get("en") }
            )
        },
        issued = issued,
        modified = modified,
        language = language?.mapNotNull { it.uri },
        landingPage = landingPage,
        euDataTheme = allEuDataThemes(),
        losTheme = allLosThemes(),
        distribution = distribution?.map {
            DistributionDBO(
                title = it.title?.let { title -> LocalizedStrings(title.get("nb"), title.get("nn"), title.get("en")) },
                description = it.description?.let { desc ->
                    LocalizedStrings(
                        desc.get("nb"),
                        desc.get("nn"),
                        desc.get("en")
                    )
                },
                downloadURL = it.downloadURL,
                accessURL = it.accessURL,
                license = it.license?.uri,
                conformsTo = it.conformsTo?.map { concept -> UriWithLabel(concept.uri, concept.prefLabel) },
                page = it.page?.mapNotNull { page -> page.uri },
                format = it.format,
                mediaType = it.mediaType,
                accessServices = it.allAccessServiceUris()
            )
        },
        sample = sample?.map {
            DistributionDBO(
                title = it.title?.let { title -> LocalizedStrings(title.get("nb"), title.get("nn"), title.get("en")) },
                description = it.description?.let { desc ->
                    LocalizedStrings(
                        desc.get("nb"),
                        desc.get("nn"),
                        desc.get("en")
                    )
                },
                downloadURL = it.downloadURL,
                accessURL = it.accessURL,
                license = it.license?.uri,
                conformsTo = it.conformsTo?.map { standard -> UriWithLabel(standard.uri, standard.prefLabel) },
                page = it.page?.mapNotNull { page -> page.uri },
                format = it.format,
                mediaType = it.mediaType,
                accessServices = it.allAccessServiceUris()
            )
        },
        temporal = temporal?.map {
            PeriodOfTimeDBO(
                startDate = it.startDate,
                endDate = it.endDate
            )
        },
        spatial = spatial?.mapNotNull { it.uri },
        accessRight = accessRights?.uri,
        legalBasisForRestriction = legalBasisForRestriction?.map { UriWithLabel(it.uri, it.prefLabel) },
        legalBasisForProcessing = legalBasisForProcessing?.map { UriWithLabel(it.uri, it.prefLabel) },
        legalBasisForAccess = legalBasisForAccess?.map { UriWithLabel(it.uri, it.prefLabel) },
        accuracy = hasAccuracyAnnotation?.let {
            QualityAnnotationDBO(
                inDimension = it.inDimension,
                motivatedBy = it.motivatedBy,
                hasBody = it.hasBody
            )
        },
        completeness = hasCompletenessAnnotation?.let {
            QualityAnnotationDBO(
                inDimension = it.inDimension,
                motivatedBy = it.motivatedBy,
                hasBody = it.hasBody
            )
        },
        currentness = hasCurrentnessAnnotation?.let {
            QualityAnnotationDBO(
                inDimension = it.inDimension,
                motivatedBy = it.motivatedBy,
                hasBody = it.hasBody
            )
        },
        availability = hasAvailabilityAnnotation?.let {
            QualityAnnotationDBO(
                inDimension = it.inDimension,
                motivatedBy = it.motivatedBy,
                hasBody = it.hasBody
            )
        },
        relevance = hasRelevanceAnnotation?.let {
            QualityAnnotationDBO(
                inDimension = it.inDimension,
                motivatedBy = it.motivatedBy,
                hasBody = it.hasBody
            )
        },
        references = references?.map {
            ReferenceDBO(
                referenceType = it.referenceType?.code,
                source = it.source?.uri
            )
        },
        relatedResources = relations?.map { UriWithLabel(it.uri, it.prefLabel) },
        provenance = provenance?.uri,
        frequency = accrualPeriodicity?.uri,
        conformsTo = conformsTo?.map { UriWithLabel(it.uri, it.prefLabel) },
        informationModelsFromOtherSources = informationModel?.map { UriWithLabel(it.uri, it.prefLabel) },
        informationModelsFromFDK = informationModelsFromFDK,
        qualifiedAttributions = qualifiedAttributions,
        type = type,
        inSeries = inSeries,
        seriesDatasetOrder = seriesDatasetOrder
    )

fun QualityAnnotationDBO.toQualityAnnotation(): QualityAnnotation =
    QualityAnnotation(hasBody = hasBody, inDimension = inDimension, motivatedBy = motivatedBy)

fun DatasetDBO.toDataset(): Dataset {
    return Dataset(
        id = id,
        catalogId = catalogId,
        uri = uri,

        specializedType = specializedType,
        originalUri = originalUri,
        lastModified = lastModified,
        registrationStatus = when {
            published -> REGISTRATION_STATUS.PUBLISH
            approved -> REGISTRATION_STATUS.APPROVE
            else -> REGISTRATION_STATUS.DRAFT
        },
        concepts = concepts?.map { Concept(uri = it) },
        title = title?.toMap(),
        description = description?.toMap(),
        contactPoint = contactPoints?.map {
            Contact(
                organizationUnit = it.name?.nb,
                email = it.email,
                hasURL = it.url,
                hasTelephone = it.phone
            )
        },
        keyword = keywords?.toKeywordList(),
        issued = issued,
        modified = modified,
        language = language?.map { SkosCode(uri = it, code = it.split("/").last()) },
        landingPage = landingPage,
        losTheme = losTheme,
        euDataTheme = euDataTheme,
        theme = oldThemeList(),
        distribution = distribution?.map {
            Distribution(
                title = it.title?.toMap(),
                description = it.description?.toMap(),
                downloadURL = it.downloadURL,
                accessURL = it.accessURL,
                license = SkosConcept(uri = it.license),
                conformsTo = it.conformsTo?.map { uri -> SkosConcept(uri = uri.uri, prefLabel = uri.prefLabel) },
                format = it.format,
                mediaType = it.mediaType,
                accessServiceUris = it.accessServices,
                accessService = it.accessServices?.map { uri -> DataDistributionService(uri = uri) },
                page = it.page?.map { uri -> SkosConcept(uri = uri) }

            )
        },
        sample = sample?.map {
            Distribution(
                title = it.title?.toMap(),
                description = it.description?.toMap(),
                downloadURL = it.downloadURL,
                accessURL = it.accessURL,
                format = it.format,
                mediaType = it.mediaType,
                page = it.page?.map { uri -> SkosConcept(uri = uri) }
            )
        },
        temporal = temporal?.map {
            PeriodOfTime(
                startDate = it.startDate,
                endDate = it.endDate
            )
        },
        spatial = spatial?.map { SkosCode(uri = it) },
        accessRights = accessRight?.let { SkosCode(uri = it) },
        legalBasisForRestriction = legalBasisForRestriction?.map {
            SkosConcept(
                uri = it.uri,
                prefLabel = it.prefLabel
            )
        },
        legalBasisForProcessing = legalBasisForProcessing?.map { SkosConcept(uri = it.uri, prefLabel = it.prefLabel) },
        legalBasisForAccess = legalBasisForAccess?.map { SkosConcept(uri = it.uri, prefLabel = it.prefLabel) },
        hasAccuracyAnnotation = accuracy?.toQualityAnnotation(),
        hasCompletenessAnnotation = completeness?.toQualityAnnotation(),
        hasCurrentnessAnnotation = currentness?.toQualityAnnotation(),
        hasAvailabilityAnnotation = availability?.toQualityAnnotation(),
        hasRelevanceAnnotation = relevance?.toQualityAnnotation(),
        references = references?.map {
            Reference(
                referenceType = SkosCode(code = it.referenceType),
                source = SkosConcept(uri = it.source)
            )
        },
        relations = relatedResources?.map { SkosConcept(uri = it.uri, prefLabel = it.prefLabel) },
        provenance = provenance?.let { SkosCode(uri = it) },
        accrualPeriodicity = frequency?.let { SkosCode(uri = it) },
        conformsTo = conformsTo?.map { SkosConcept(uri = it.uri, prefLabel = it.prefLabel) },
        informationModelsFromFDK = informationModelsFromFDK,
        informationModel = informationModelsFromOtherSources?.map { model ->
            SkosConcept(
                uri = model.uri,
                prefLabel = model.prefLabel
            )
        },
        qualifiedAttributions = qualifiedAttributions,
        type = type,
        inSeries = inSeries,
        seriesDatasetOrder = seriesDatasetOrder
    )
}

fun LocalizedStrings.toMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    nb?.let { map["nb"] = it }
    nn?.let { map["nn"] = it }
    en?.let { map["en"] = it }
    return map
}

fun LocalizedStringLists.toKeywordList(): List<Map<String, String>> {
    val keywordList = mutableListOf<Map<String, String>>()

    val maxSize = listOf(nb?.size ?: 0, nn?.size ?: 0, en?.size ?: 0).maxOrNull() ?: 0

    for (i in 0 until maxSize) {
        val localizedStrings = LocalizedStrings(
            nb = nb?.getOrNull(i),
            nn = nn?.getOrNull(i),
            en = en?.getOrNull(i)
        )

        val map = localizedStrings.toMap()

        if (map.isNotEmpty()) {
            keywordList.add(map)
        }
    }

    return keywordList
}

fun DatasetDBO.oldThemeList(): List<DataTheme>? {
    val result: MutableList<DataTheme> = mutableListOf()
    losTheme?.forEach { theme -> result.add(DataTheme(uri = theme)) }
    euDataTheme?.forEach { theme -> result.add(DataTheme(uri = theme)) }

    return if (result.size > 0) result else null
}

fun Dataset.allLosThemes(): Set<String>? {
    val result: MutableSet<String> = losTheme?.toMutableSet() ?: mutableSetOf()
    theme?.forEach { item -> if (item.uri != null && item.uri.startsWith("https://psi.norge.no/los")) result.add(item.uri) }
    return if (result.size > 0) result else null
}

fun Dataset.allEuDataThemes(): Set<String>? {
    val result: MutableSet<String> = euDataTheme?.toMutableSet() ?: mutableSetOf()
    theme?.forEach { item ->
        if (item.uri != null && item.uri.startsWith("http://publications.europa.eu/resource/authority/data-theme")) result.add(
            item.uri
        )
    }
    return if (result.size > 0) result else null
}


fun Distribution.allAccessServiceUris(): Set<String>? {
    val result: MutableSet<String> = accessServiceUris?.toMutableSet() ?: mutableSetOf()
    accessService?.forEach { item -> if (item.uri != null) result.add(item.uri) }
    return if (result.size > 0) result else null
}