package no.fdk.dataset_catalog.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document( collection = "datasets" )
data class Dataset(
        @Id val id: String,
        val catalogId: String
)