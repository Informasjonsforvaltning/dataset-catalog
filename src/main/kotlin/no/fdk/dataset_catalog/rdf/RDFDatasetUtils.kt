package no.fdk.dataset_catalog.rdf

import no.fdk.dataset_catalog.model.Dataset
import org.apache.jena.rdf.model.Model

fun Model.createDatasetResource(dataset: Dataset): Model {
//    TODO: add properties
    createResource(dataset.originalUri ?: dataset.uri)
    return this
}