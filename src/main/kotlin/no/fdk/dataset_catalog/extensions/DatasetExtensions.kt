package no.fdk.dataset_catalog.extensions

import no.fdk.dataset_catalog.model.*
import org.apache.jena.vocabulary.DCTerms

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
                name = it.name?.toLocalizedStrings(),
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
                conformsTo = it.conformsTo?.map { concept -> UriWithLabel(concept.uri, concept.prefLabel?.toLocalizedStrings()) },
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
                conformsTo = it.conformsTo?.map { standard -> UriWithLabel(standard.uri, standard.prefLabel?.toLocalizedStrings()) },
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
        legalBasisForRestriction = legalBasisForRestriction?.map { UriWithLabel(it.uri, it.prefLabel?.toLocalizedStrings()) },
        legalBasisForProcessing = legalBasisForProcessing?.map { UriWithLabel(it.uri, it.prefLabel?.toLocalizedStrings()) },
        legalBasisForAccess = legalBasisForAccess?.map { UriWithLabel(it.uri, it.prefLabel?.toLocalizedStrings()) },
        accuracy = hasAccuracyAnnotation?.let {
            QualityAnnotationDBO(
                motivatedBy = it.motivatedBy,
                hasBody = it.hasBody?.toLocalizedStrings()
            )
        },
        completeness = hasCompletenessAnnotation?.let {
            QualityAnnotationDBO(
                motivatedBy = it.motivatedBy,
                hasBody = it.hasBody?.toLocalizedStrings()
            )
        },
        currentness = hasCurrentnessAnnotation?.let {
            QualityAnnotationDBO(
                motivatedBy = it.motivatedBy,
                hasBody = it.hasBody?.toLocalizedStrings()
            )
        },
        availability = hasAvailabilityAnnotation?.let {
            QualityAnnotationDBO(
                motivatedBy = it.motivatedBy,
                hasBody = it.hasBody?.toLocalizedStrings()
            )
        },
        relevance = hasRelevanceAnnotation?.let {
            QualityAnnotationDBO(
                motivatedBy = it.motivatedBy,
                hasBody = it.hasBody?.toLocalizedStrings()
            )
        },
        references = references?.map {
            ReferenceDBO(
                referenceType = it.referenceType?.code,
                source = it.source?.uri
            )
        },
        relatedResources = relations?.map { UriWithLabel(it.uri, it.prefLabel?.toLocalizedStrings()) },
        provenance = provenance?.uri,
        frequency = accrualPeriodicity?.uri,
        conformsTo = conformsTo?.map { UriWithLabel(it.uri, it.prefLabel?.toLocalizedStrings()) },
        informationModelsFromOtherSources = informationModel?.map { UriWithLabel(it.uri, it.prefLabel?.toLocalizedStrings()) },
        informationModelsFromFDK = informationModelsFromFDK,
        qualifiedAttributions = qualifiedAttributions,
        type = type,
        inSeries = inSeries,
        seriesDatasetOrder = seriesDatasetOrder
    )

fun QualityAnnotationDBO.toQualityAnnotation(): QualityAnnotation =
    QualityAnnotation(hasBody = hasBody?.toMap(), motivatedBy = motivatedBy)

fun DatasetDBO.toDataset(): Dataset {
    return Dataset(
        id = id,
        catalogId = catalogId,
        uri = uri,

        specializedType = specializedType,
        originalUri = originalUri,
        lastModified = lastModified,
        registrationStatus = when {
            published == true -> REGISTRATION_STATUS.PUBLISH
            approved == true -> REGISTRATION_STATUS.APPROVE
            else -> REGISTRATION_STATUS.DRAFT
        },
        concepts = concepts?.map { Concept(uri = it) },
        title = title?.toMap(),
        description = description?.toMap(),
        contactPoint = contactPoints?.map {
            Contact(
                name = it.name?.toMap(),
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
                conformsTo = it.conformsTo?.map { uri -> SkosConcept(uri = uri.uri, prefLabel = uri.prefLabel?.toMap()) },
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
                conformsTo = it.conformsTo?.map { uri -> SkosConcept(uri = uri.uri, prefLabel = uri.prefLabel?.toMap()) },
                format = it.format,
                mediaType = it.mediaType,
                accessServiceUris = it.accessServices,
                accessService = it.accessServices?.map { uri -> DataDistributionService(uri = uri) },
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
                prefLabel = it.prefLabel?.toMap()
            )
        },
        legalBasisForProcessing = legalBasisForProcessing?.map { SkosConcept(uri = it.uri, prefLabel = it.prefLabel?.toMap()) },
        legalBasisForAccess = legalBasisForAccess?.map { SkosConcept(uri = it.uri, prefLabel = it.prefLabel?.toMap()) },
        hasAccuracyAnnotation = accuracy?.toQualityAnnotation(),
        hasCompletenessAnnotation = completeness?.toQualityAnnotation(),
        hasCurrentnessAnnotation = currentness?.toQualityAnnotation(),
        hasAvailabilityAnnotation = availability?.toQualityAnnotation(),
        hasRelevanceAnnotation = relevance?.toQualityAnnotation(),
        references = references?.map {
            Reference(
                referenceType = it.asSkosCode(),
                source = SkosConcept(uri = it.source),
            )
        },
        relations = relatedResources?.map { SkosConcept(uri = it.uri, prefLabel = it.prefLabel?.toMap()) },
        provenance = provenance?.let { SkosCode(uri = it) },
        accrualPeriodicity = frequency?.let { SkosCode(uri = it) },
        conformsTo = conformsTo?.map { SkosConcept(uri = it.uri, prefLabel = it.prefLabel?.toMap()) },
        informationModelsFromFDK = informationModelsFromFDK,
        informationModel = informationModelsFromOtherSources?.map { model ->
            SkosConcept(
                uri = model.uri,
                prefLabel = model.prefLabel?.toMap()
            )
        },
        qualifiedAttributions = qualifiedAttributions,
        type = type,
        inSeries = inSeries,
        seriesDatasetOrder = seriesDatasetOrder
    )
}

fun DatasetDBO.addCreateValues(toCreate: DatasetToCreate) =
    copy(
        approved = toCreate.approved,
        originalUri = toCreate.originalUri,
        specializedType = toCreate.specializedType,
        concepts = toCreate.concepts,
        title = toCreate.title,
        description = toCreate.description,
        contactPoints = toCreate.contactPoints,
        keywords = toCreate.keywords,
        issued = toCreate.issued,
        modified = toCreate.modified,
        language = toCreate.language,
        landingPage = toCreate.landingPage,
        euDataTheme = toCreate.euDataTheme,
        losTheme = toCreate.losTheme,
        distribution = toCreate.distribution,
        sample = toCreate.sample,
        temporal = toCreate.temporal,
        spatial = toCreate.spatial,
        accessRight = toCreate.accessRight,
        legalBasisForRestriction = toCreate.legalBasisForRestriction,
        legalBasisForProcessing = toCreate.legalBasisForProcessing,
        legalBasisForAccess = toCreate.legalBasisForAccess,
        accuracy = toCreate.accuracy,
        completeness = toCreate.completeness,
        currentness = toCreate.currentness,
        availability = toCreate.availability,
        relevance = toCreate.relevance,
        references = toCreate.references,
        relatedResources = toCreate.relatedResources,
        provenance = toCreate.provenance,
        frequency = toCreate.frequency,
        conformsTo = toCreate.conformsTo,
        informationModelsFromFDK = toCreate.informationModelsFromFDK,
        informationModelsFromOtherSources = toCreate.informationModelsFromOtherSources,
        qualifiedAttributions = toCreate.qualifiedAttributions,
        type = toCreate.type,
        inSeries = toCreate.inSeries,
        seriesDatasetOrder = toCreate.seriesDatasetOrder,
    )

fun LocalizedStrings.toMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    nb?.let { map["nb"] = it }
    nn?.let { map["nn"] = it }
    en?.let { map["en"] = it }
    return map
}

fun Map<String, String>.toLocalizedStrings(): LocalizedStrings {
    return LocalizedStrings(
        nb = this["nb"],
        nn = this["nn"],
        en = this["en"]
    )
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

private fun ReferenceDBO.asSkosCode(): SkosCode? {
    if (referenceType != null) {
        return SkosCode(
            uri = DCTerms.getURI() + referenceType,
            code = referenceType,
            prefLabel = mapOf(Pair("en", referenceType))
        )
    } else return null
}
