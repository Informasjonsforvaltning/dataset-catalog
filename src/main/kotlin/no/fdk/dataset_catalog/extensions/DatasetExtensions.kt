package no.fdk.dataset_catalog.extensions

import no.fdk.dataset_catalog.model.*

fun List<Dataset>.toDTO() : DatasetEmbeddedWrapperDTO = DatasetEmbeddedWrapperDTO(mapOf(Pair("datasets", this)))
