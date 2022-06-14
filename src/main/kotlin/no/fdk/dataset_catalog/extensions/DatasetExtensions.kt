package no.fdk.dataset_catalog.extensions

import no.fdk.dataset_catalog.model.*

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
